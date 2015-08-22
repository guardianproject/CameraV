package org.witness.informacam.models.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.witness.informacam.models.Model;

@SuppressWarnings("serial")
public class ILanguageMap extends Model implements Serializable {
	public List<ILanguage> languages = new ArrayList<ILanguage>();
	
	public ILanguageMap() {}
	
	public void add(String code, String label) {
		languages.add(languages.size(), new ILanguage(code, label));
	}
	
	public String getCode(int which) {
		return languages.get(which).code;
	}
	
	public ArrayList<String> getLabels() {
		ArrayList<String> labels = new ArrayList<String>();
		for(ILanguage language : languages) {
			labels.add(language.label);
		}
		
		return labels;
	}
}
