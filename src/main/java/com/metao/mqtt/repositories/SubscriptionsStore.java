package com.metao.mqtt.repositories;

import com.metao.mqtt.models.ClientTopicCouple;
import com.metao.mqtt.models.Subscription;
import com.metao.mqtt.models.Token;
import com.metao.mqtt.models.TreeSpace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicReference;

public class SubscriptionsStore {
    private static final Logger LOG = LoggerFactory.getLogger(SubscriptionsStore.class);

    public static class NodeCouple {
        final TreeSpace root;
        final TreeSpace createdNode;

        public NodeCouple(TreeSpace root, TreeSpace createdNode) {
            this.root = root;
            this.createdNode = createdNode;
        }
    }

    /**
     * Check if the topic filter of the subscription is well formed
     */
    public static boolean validate(String topicFilter) {
        try {
            parseTopic(topicFilter);
            return true;
        } catch (ParseException pex) {
            LOG.info("Bad matching topic filter <{}>", topicFilter);
            return false;
        }
    }

    public interface IVisitor<T> {
        void visit(TreeSpace node, int deep);

        T getResult();
    }

    private class DumpTreeVisitor implements IVisitor<String> {

        String s = "";

        @Override
        public void visit(TreeSpace node, int deep) {
            String subScriptionsStr = "";
            String indentTabs = indentTabs(deep);
            for (ClientTopicCouple couple : node.clients) {
                subScriptionsStr += indentTabs + couple.toString() + "\n";
            }
            s += node.getToken() == null ? "" : node.getToken().toString();
            s += "\n" + (node.clients.isEmpty() ? indentTabs : "") + subScriptionsStr;
        }

        private String indentTabs(int deep) {
            String s = "";
            for (int i = 0; i < deep; i++) {
                s += "\t";
            }
            return s;
        }

        @Override
        public String getResult() {
            return s;
        }
    }

    private AtomicReference<TreeSpace> topicTree = new AtomicReference<>(new TreeSpace());
    private volatile SessionsStore sessionsStore;

    /**
     * Initialize the subscription topicTree with the list of getClients.
     * Maintained for compatibility reasons.
     */
    public void init(SessionsStore sessionsStore) {
        LOG.debug("init invoked");
        this.sessionsStore = sessionsStore;
        List<ClientTopicCouple> subscriptions = sessionsStore.listAllSubscriptions();
        //reload any getClients persisted
        if (LOG.isDebugEnabled()) {
            LOG.debug("Reloading all stored getClients...subscription topicTree before {}", dumpTree());
        }

        for (ClientTopicCouple clientTopic : subscriptions) {
            LOG.debug("Re-subscribing {} to topic {}", clientTopic.clientId, clientTopic.topicFilter);
            add(clientTopic);
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("Finished loading. Subscription topicTree after {}", dumpTree());
        }
    }

    public void add(ClientTopicCouple newSubscription) {
        TreeSpace oldRoot;
        NodeCouple couple;
        do {
            oldRoot = topicTree.get();
            couple = recreatePath(newSubscription.topicFilter, oldRoot);
            couple.createdNode.addClient(newSubscription); //createdNode could be null?
            //spin lock repeating till we can, swap root, if can't swap just re-do the operation
        } while (!topicTree.compareAndSet(oldRoot, couple.root));
        LOG.debug("root ref {}, original root was {}", couple.root, oldRoot);
    }


    protected NodeCouple recreatePath(String topic, final TreeSpace oldRoot) {
        List<Token> tokens = new ArrayList<>();
        try {
            tokens = parseTopic(topic);
        } catch (ParseException ex) {
            LOG.error("error topic", ex);
        }

        final TreeSpace newRoot = oldRoot.copy();
        TreeSpace parent = newRoot;
        TreeSpace current = newRoot;
        for (Token token : tokens) {
            TreeSpace match;

            //check if a children with the same token already exists
            if ((match = current.childWithToken(token)) != null) {
                //copy the traversed node
                current = match.copy();
                //update the child just added in the children list
                parent.updateChild(match, current);
                parent = current;
            } else {
                //create a new node for the newly inserted token
                match = new TreeSpace();
                match.setToken(token);
                current.addChild(match);
                current = match;
            }
        }
        return new NodeCouple(newRoot, current);
    }

    public void removeSubscription(String topic, String clientID) {
        TreeSpace oldRoot;
        NodeCouple couple;
        do {
            oldRoot = topicTree.get();
            couple = recreatePath(topic, oldRoot);

            couple.createdNode.remove(new ClientTopicCouple(clientID, topic));
            //spin lock repeating till we can, swap root, if can't swap just re-do the operation
        } while (!topicTree.compareAndSet(oldRoot, couple.root));
    }

    /**
     * Visit the topics topicTree to remove matching getClients with clientId.
     * It's a mutating structure operation so create a new subscription topicTree (partial or total).
     */
    public void removeForClient(String clientID) {
        TreeSpace oldRoot;
        TreeSpace newRoot;
        do {
            oldRoot = topicTree.get();
            newRoot = oldRoot.removeClientSubscriptions(clientID);
            //spin lock repeating till we can, swap root, if can't swap just re-do the operation
        } while (!topicTree.compareAndSet(oldRoot, newRoot));
    }


    /**
     * Given a topic string return the getClients getClients that matches it.
     * Topic string can't contain character # and + because they are reserved to
     * listeners getClients, and not topic publishing.
     */
    public List<Subscription> matches(String topic) {
        List<Token> tokens;
        try {
            tokens = parseTopic(topic);
        } catch (ParseException ex) {
            LOG.error("error topic", ex);
            return Collections.emptyList();
        }

        Queue<Token> tokenQueue = new LinkedBlockingDeque<>(tokens);
        List<ClientTopicCouple> matchClients = new ArrayList<>();
        topicTree.get().matches(tokenQueue, matchClients);

        //remove the overlapping getClients, selecting ones with greatest qos
        Map<String, Subscription> subsForClient = new HashMap<>();
        for (ClientTopicCouple matchingCouple : matchClients) {
            Subscription existingSub = subsForClient.get(matchingCouple.clientId);
            Subscription sub = sessionsStore.getSubscription(matchingCouple);
            if (sub == null) {
                //if the sessionStore hasn't the sub because the client disconnected
                continue;
            }
            //update the selected getClients if not present or if has a greater qos
            if (existingSub == null || existingSub.getRequestedQos().byteValue() < sub.getRequestedQos().byteValue()) {
                subsForClient.put(matchingCouple.clientId, sub);
            }
        }
        return new ArrayList<>(subsForClient.values());
    }

    public boolean contains(Subscription sub) {
        return !matches(sub.getTopicFilter()).isEmpty();
    }

    public int size() {
        return topicTree.get().size();
    }

    public String dumpTree() {
        DumpTreeVisitor visitor = new DumpTreeVisitor();
        bfsVisit(topicTree.get(), visitor, 0);
        return visitor.getResult();
    }

    private void bfsVisit(TreeSpace node, IVisitor visitor, int deep) {
        if (node == null) {
            return;
        }
        visitor.visit(node, deep);
        for (TreeSpace child : node.children) {
            bfsVisit(child, visitor, ++deep);
        }
    }

    /**
     * Verify if the 2 topics matching respecting the rules of MQTT Appendix A
     */
    //TODO reimplement with iterators or with queues
    public static boolean matchTopics(String msgTopic, String subscriptionTopic) {
        try {
            List<Token> msgTokens = SubscriptionsStore.parseTopic(msgTopic);
            List<Token> subscriptionTokens = SubscriptionsStore.parseTopic(subscriptionTopic);
            int i = 0;
            for (; i < subscriptionTokens.size(); i++) {
                Token subToken = subscriptionTokens.get(i);
                if (subToken != Token.MULTI && subToken != Token.SINGLE) {
                    if (i >= msgTokens.size()) {
                        return false;
                    }
                    Token msgToken = msgTokens.get(i);
                    if (!msgToken.equals(subToken)) {
                        return false;
                    }
                } else {
                    if (subToken == Token.MULTI) {
                        return true;
                    }
                    if (subToken == Token.SINGLE) {
                        //skip a step forward
                    }
                }
            }

            return i == msgTokens.size();
        } catch (ParseException ex) {
            LOG.error(null, ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * 解析主题
     *
     * @param topic
     * @return
     * @throws ParseException
     */
    protected static List<Token> parseTopic(String topic) throws ParseException {
        List<Token> res = new ArrayList<>();
        String[] splitted = topic.split("/");

        if (splitted.length == 0) {
            res.add(Token.EMPTY);
        }

        if (topic.endsWith("/")) {
            //Add a fictious space
            String[] newSplitted = new String[splitted.length + 1];
            System.arraycopy(splitted, 0, newSplitted, 0, splitted.length);
            newSplitted[splitted.length] = "";
            splitted = newSplitted;
        }

        for (int i = 0; i < splitted.length; i++) {
            String s = splitted[i];
            if (s.isEmpty()) {
                res.add(Token.EMPTY);
            } else if (s.equals("#")) {
                //check that multi is the last symbol
                if (i != splitted.length - 1) {
                    throw new ParseException("Bad format of topic, the multi symbol (#) has to be the last one after a separator", i);
                }
                res.add(Token.MULTI);
            } else if (s.contains("#")) {
                throw new ParseException("Bad format of topic, invalid subtopic name: " + s, i);
            } else if (s.equals("+")) {
                res.add(Token.SINGLE);
            } else if (s.contains("+")) {
                throw new ParseException("Bad format of topic, invalid subtopic name: " + s, i);
            } else {
                res.add(new Token(s));
            }
        }

        return res;
    }
}
