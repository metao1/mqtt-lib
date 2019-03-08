package de.vispiron.carsync.mqtt.models;

/**
 * @author Mehrdad A.Karami at 2/26/19
 **/

public enum QosType {MOST_ONE, LEAST_ONE, EXACTLY_ONCE, RESEREVED, FAILURE;

	public static QosType valueOf(byte qos) {
		switch (qos) {
		case 0x00:
			return MOST_ONE;
		case 0x01:
			return LEAST_ONE;
		case 0x02:
			return EXACTLY_ONCE;
		case (byte) 0x80:
			return FAILURE;
		default:
			throw new IllegalArgumentException(
					String.format("Invalid QoS Type, Expected 1,2,0 or 0x80, expected {%s} ", qos));
		}
	}
	public byte byteValue(){
		switch (this){
		case MOST_ONE:
			return 0;
		case FAILURE:
			return (byte) 0x80;
		case LEAST_ONE:
			return 2;
		case EXACTLY_ONCE:
			return 1;
			default:
				throw new IllegalArgumentException("Cann't give byteValue of QosType:"+ this.name());
		}
	}

}

