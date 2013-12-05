package net.sf.relish.configprops;

import java.util.List;

import net.sf.relish.NameValuePair;

import cucumber.api.java.en.Given;

/**
 * Cucumber step defs for the config properties feature
 */
public final class ConfigPropsStepDefs {

	/**
	 * For each name/value pair this sets the system property with the name to the value. System properties are used as application configuration properties.
	 */
	@Given("^config properties are:$")
	public void configPropertiesAre(List<NameValuePair> properties) {

		for (NameValuePair prop : properties) {
			System.setProperty(prop.getName(), prop.getValue());
		}
	}

	/**
	 * Sets the system property with the name to the value. System properties are used as application configuration properties.
	 */
	@Given("^config property \"(\\S.*)\" is \"(\\S.*)\"$")
	public void configPropertyis(String name, String value) {

		System.setProperty(name, value);
	}
}
