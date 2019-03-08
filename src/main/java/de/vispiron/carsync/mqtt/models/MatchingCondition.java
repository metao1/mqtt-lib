package de.vispiron.carsync.mqtt.models;

/**
 * @author Mehrdad A.Karami at 3/5/19
 **/

public interface MatchingCondition {

	boolean match(String key);
}
