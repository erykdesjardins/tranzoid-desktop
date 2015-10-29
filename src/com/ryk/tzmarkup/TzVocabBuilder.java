package com.ryk.tzmarkup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

import android.util.Log;

import com.ryk.tzclientlib.TzPreferences;

public final class TzVocabBuilder {
	private TzVocabBuilder() {}
	
	public static HashMap<String, String> getLanguage(String language) {
		HashMap<String, String> map = new HashMap<String, String>();
		
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(TzPreferences.mainCtx.getAssets().open("vocab/"+language+".vocab"), "UTF-8"));
		
			String data = reader.readLine();
			while (data != null) {
				String[] relation = data.split("\\|");
				map.put(relation[0], relation[1]);
				
				data = reader.readLine();
			}
		
			reader.close();
		} catch (IOException e) {
			Log.e("ryk", "Error building English dicrionary | IOException");
		}
		
		return map;
	}
}
