package org.witness.informacam.models.utils;

import java.io.Serializable;

import org.witness.informacam.models.Model;

@SuppressWarnings("serial")
public class ILanguage extends Model implements Serializable {
	public String code = null;
	public String label = null;
	
	public ILanguage() {}
	
	public ILanguage(String code, String label) {
		this.code = code;
		this.label = label;
	}
}
