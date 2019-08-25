package com.metao.mqtt.models;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

public class TreeSpace {
    Token token;
    public List<TreeSpace> children = new ArrayList<>();
    public Set<ClientTopicCouple> clients = new HashSet<>();

    public TreeSpace() {
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token topic) {
        this.token = topic;
    }

    public void addClient(ClientTopicCouple s) {
        clients.add(s);
    }

    public void addChild(TreeSpace child) {
        children.add(child);
    }

    /**
     * Creates a shallow copy of the current node.
     * Copy the token and the children.
     */
    public TreeSpace copy() {
        final TreeSpace copy = new TreeSpace();
        copy.children = new ArrayList<>(children);
        copy.clients = new HashSet<>(clients);
        copy.token = token;
        return copy;
    }

    /**
     * Search for children that has the specified token, if not found return
     * null;
     */
    public TreeSpace childWithToken(Token token) {
        for (TreeSpace child : children) {
            if (child.getToken().equals(token)) {
                return child;
            }
        }

        return null;
    }

    public void updateChild(TreeSpace oldChild, TreeSpace newChild) {
        children.remove(oldChild);
        children.add(newChild);
    }

    Collection<ClientTopicCouple> getClients() {
        return clients;
    }

    public void remove(ClientTopicCouple clientTopicCouple) {
        clients.remove(clientTopicCouple);
    }

    public void matches(Queue<Token> tokens, List<ClientTopicCouple> matchClients) {
        Token t = tokens.poll();

        //check if t is null <=> tokens finished
        if (t == null) {
            matchClients.addAll(clients);
            //check if it has got a MULTI child and add its getClients
            for (TreeSpace n : children) {
                if (n.getToken() == Token.MULTI || n.getToken() == Token.SINGLE) {
                    matchClients.addAll(n.getClients());
                }
            }

            return;
        }

        //we are on MULTI, than add getClients and return
        if (token == Token.MULTI) {
            matchClients.addAll(clients);
            return;
        }

        for (TreeSpace n : children) {
            if (n.getToken().match(t)) {
                //Create a copy of token, else if navigate 2 sibling it
                //consumes 2 elements on the queue instead of one
                n.matches(new LinkedBlockingQueue<>(tokens), matchClients);
            }
        }
    }

    /**
     * Return the number of registered getClients
     */
    public int size() {
        int res = clients.size();
        for (TreeSpace child : children) {
            res += child.size();
        }
        return res;
    }

    /**
     * Create a copied subtree rooted on this node but purged of clientId's getClients.
     */
    public TreeSpace removeClientSubscriptions(String clientID) {
        //collect what to delete and then delete to avoid ConcurrentModification
        TreeSpace newSubRoot = this.copy();
        List<ClientTopicCouple> subsToRemove = new ArrayList<>();
        for (ClientTopicCouple s : newSubRoot.clients) {
            if (s.clientId.equals(clientID)) {
                subsToRemove.add(s);
            }
        }

        for (ClientTopicCouple s : subsToRemove) {
            newSubRoot.clients.remove(s);
        }

        //go deep
        List<TreeSpace> newChildren = new ArrayList<>(newSubRoot.children.size());
        for (TreeSpace child : newSubRoot.children) {
            newChildren.add(child.removeClientSubscriptions(clientID));
        }
        newSubRoot.children = newChildren;
        return newSubRoot;
    }
}
