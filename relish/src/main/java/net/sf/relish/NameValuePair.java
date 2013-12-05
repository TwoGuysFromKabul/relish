package net.sf.relish;

/**
 * A name/value pair. This class is thread safe.
 */
public final class NameValuePair {

	private final String name;
	private final String value;

	public NameValuePair(String name, String value) {
		this.name = name;
		this.value = value;
	}

	/**
	 * @return The header's name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return The header's value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return name + "=" + value;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		NameValuePair other = (NameValuePair) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (value == null) {
			if (other.value != null) {
				return false;
			}
		} else if (!value.equals(other.value)) {
			return false;
		}
		return true;
	}

}
