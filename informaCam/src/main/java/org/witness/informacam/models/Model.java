package org.witness.informacam.models;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.witness.informacam.InformaCam;
import org.witness.informacam.json.JSONArray;
import org.witness.informacam.json.JSONException;
import org.witness.informacam.json.JSONObject;
import org.witness.informacam.json.JSONTokener;
import org.witness.informacam.utils.Constants.App;
import org.witness.informacam.utils.Constants.App.Informa;

import android.util.Log;

public class Model extends JSONObject {
	public final static String LOG = App.LOG;
	Field[] fields;

	public void inflate(byte[] jsonStringBytes) throws InstantiationException, IllegalAccessException {
		try {
			if(jsonStringBytes != null) {
				inflate((JSONObject) new JSONTokener(new String(jsonStringBytes)).nextValue());
			} else {
				Log.d(LOG, "json is null, no inflate");
			}
		} catch (JSONException e) {
			Log.e(LOG, e.toString());
			e.printStackTrace();
		} catch(NullPointerException e) {
			Log.e(LOG, e.toString());
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public Class<?> recast(Object m, JSONObject ja) {
		InformaCam informaCam = InformaCam.getInstance();

		Set<Map<Class<?>, List<String>>> subclasses = new HashSet<Map<Class<?>, List<String>>>();
		
		Class<?> clz = m.getClass();
		Class<?> recast = null;

		String packagePath = clz.getName().replace(("." + clz.getSimpleName()), "");

		for(String model : informaCam.models) {
			if(model.contains(packagePath) && !model.equals(clz.getName())) {
				try {
					Class<?> subClz = Class.forName(model);
					if(subClz.getSuperclass().equals(clz)) {
						//Log.d(LOG, "adding " + model + " as possible subclass for " + clz.getName());
						
						List<String> fieldSet = new ArrayList<String>();
						for(Field subField : subClz.getDeclaredFields()) {
							if(subField.getModifiers() == Field.DECLARED) {
								fieldSet.add(subField.getName());
							}
						}
						Map<Class<?>, List<String>> subClz_ = new HashMap<Class<?>, List<String>>();
						subClz_.put(subClz, fieldSet);
						
						subclasses.add(subClz_);
					}
				} catch (ClassNotFoundException e) {
					Log.e(LOG, e.toString());
					e.printStackTrace();
				}

			}
		}

		if(subclasses.size() > 0) {			
			// loop through json to see if we have any of these fields. eliminate non-matches from list
			Iterator<String> kIt = ja.keys();
			while(kIt.hasNext()) {
				String keyToFind = kIt.next();
				int keyFoundInClasses = 0;
				
				Class<?> c = null;
				
				for(Map<Class<?>, List<String>> subClz : subclasses) {
					// does property in key set belong exclusively to
					Entry<Class<?>, List<String>> entry = subClz.entrySet().iterator().next();
					
					List<String> ls = entry.getValue();
					
					//Log.d(LOG, "parsing " + entry.getKey().getName() + " to see if it contains " + keyToFind);
					
					if(ls.contains(keyToFind)) {
						keyFoundInClasses++;
						c = entry.getKey();
					}
				}
				
				//Log.d(LOG, "found " + keyToFind + " in " + keyFoundInClasses + " class(es)");
				if(keyFoundInClasses == 1) {
					//Log.d(LOG, "downcast object to " + c.getName());
					return c;
				}
			}
		}
		
		return recast;
	}
	
	public void inflate(Model model) throws InstantiationException, IllegalAccessException {
		inflate(model.asJson());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void inflate(JSONObject values) throws InstantiationException, IllegalAccessException {
		
	//	Log.d(Informa.LOG,"Inflating model: " + getClass().getName() + " with values: " + values.toString());

		fields = this.getClass().getFields();
		for(Field f : fields) {
			
				f.setAccessible(true);
				if(values.has(f.getName())) {
					boolean isModel = false;

		//			Log.d(Informa.LOG,"inflating field: " + f.getName() + " " + f.getType().getName());
					
					if(f.getType().getSuperclass() == Model.class) {
						isModel = true;
					}					

					if(f.getType() == List.class) {
						List subValue = new ArrayList();
						Class clz = (Class<?>) ((ParameterizedType) f.getGenericType()).getActualTypeArguments()[0];

						Object test = clz.newInstance();
						if(test instanceof Model) {
							isModel = true;
						}

						JSONArray ja = values.getJSONArray(f.getName());
						for(int i=0; i<ja.length(); i++) {
							Object value = clz.newInstance();
							if(isModel) {
								Class<?> recast = recast(value, ja.getJSONObject(i));
								if(recast != null) {
									value = recast.newInstance();
								}

								((Model) value).inflate(ja.getJSONObject(i));
							} else {
								value = ja.get(i);
							}
							subValue.add(value);
						}

						f.set(this, subValue);
					} else if(f.getType() == byte[].class) { 
						f.set(this, values.getString(f.getName()).getBytes());
					} else if(f.getType() == float[].class) {
						//f.set(this, parseJSONAsFloatArray(values.getString(f.getName())));
						f.set(this, parseJSONAsFloatArray(values.getJSONArray(f.getName()).toString()));
					} else if(f.getType() == int[].class) {
						f.set(this, parseJSONAsIntArray(values.getJSONArray(f.getName()).toString()));
					} else if(isModel) {						
						Class clz = (Class<?>) f.getType();
						// if clz has less fields than the json object, this could be a subclass
						Object val = clz.newInstance();
						Class<?> recast = recast(val, values.getJSONObject(f.getName()));
						if(recast  != null) {
							val = recast.newInstance();
						}

						((Model) val).inflate(values.getJSONObject(f.getName()));
						f.set(this, val);
					} else {
						
						f.set(this, values.get(f.getName()));
					}
				}
		
		}
	}
	
	public static int[] parseJSONAsIntArray(String value) {
		String[] intStrings = value.substring(1, value.length() - 1).split(",");
		int[] ints = new int[intStrings.length];

		for(int f=0; f<intStrings.length; f++) {
			ints[f] = Integer.parseInt(intStrings[f]);
		}

		return ints;
	}

	public static JSONArray parseIntArrayAsJSON(int[] ints) {
		JSONArray intArray = new JSONArray();
		for(int f : ints) {
			intArray.put(f);
		}

		return intArray;
	}

	public static float[] parseJSONAsFloatArray(String value) {
		String[] floatStrings = value.substring(1, value.length() - 1).split(",");
		float[] floats = new float[floatStrings.length];

		for(int f=0; f<floatStrings.length; f++) {
			floats[f] = Float.parseFloat(floatStrings[f]);
		}

		return floats;
	}

	public static JSONArray parseFloatArrayAsJSON(float[] floats) {
		JSONArray floatArray = new JSONArray();
		for(float f : floats) {
			try {
				floatArray.put(f);
			} catch (JSONException e) {
				Log.e(LOG, e.toString());
				e.printStackTrace();
			}
		}

		return floatArray;
	}
	
	@SuppressWarnings("rawtypes")
	public JSONObject asJson() {
		fields = this.getClass().getFields();
		JSONObject json = new JSONObject();

		for(Field f : fields) {
			f.setAccessible(true);

			try {
				Object value = f.get(this);

				if(f.getName().contains("this$")) {
					continue;
				}

				if(f.getName().equals("NULL") || f.getName().equals("LOG")) {
					continue;
				}

				boolean isModel = false;

				if(f.getType().getSuperclass() == Model.class) {
					isModel = true;
				}

				if(f.getType() == List.class) {
					
					synchronized (value)
					{
						JSONArray subValue = new JSONArray();
						
						Iterator it = ((List<?>) value).iterator();
						
						while (it.hasNext()){
							Object v = it.next();
							
							if(v instanceof Model) {
								subValue.put(((Model) v).asJson());
							} else {
								subValue.put(v);
							}
						}

						json.put(f.getName(), subValue);
					
					}
				} else if(f.getType() == byte[].class) {
					json.put(f.getName(), new String((byte[]) value));
				} else if(f.getType() == float[].class) {
					json.put(f.getName(), parseFloatArrayAsJSON((float[]) value));
				} else if(f.getType() == int[].class) {
					json.put(f.getName(), parseIntArrayAsJSON((int[]) value));
				} else if(isModel) {
					json.put(f.getName(), ((Model) value).asJson());
				} else {
					json.put(f.getName(), value);
				}
			} catch (IllegalArgumentException e) {
				Log.d(LOG, e.toString());
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				Log.d(LOG, e.toString());
				e.printStackTrace();
			} catch (JSONException e) {
				Log.d(LOG, e.toString());
				e.printStackTrace();
			} catch (NullPointerException e) {

			}

		}

		return json;
	}
	
	@SuppressWarnings("rawtypes")
	public String asCSV() {
		fields = this.getClass().getFields();

		StringBuffer header = new StringBuffer();
		StringBuffer values = new StringBuffer();
		
		for(Field f : fields) {
			f.setAccessible(true);
			
			header.append(f.getName()).append(',');

			try {
				Object value = f.get(this);

				if(f.getName().contains("this$")) {
					continue;
				}

				if(f.getName().equals("NULL") || f.getName().equals("LOG")) {
					continue;
				}

				boolean isModel = false;

				if(f.getType().getSuperclass() == Model.class) {
					isModel = true;
				}

				if(f.getType() == List.class) {
					
					synchronized (value)
					{
						
						Iterator it = ((List<?>) value).iterator();
						
						while (it.hasNext()){
							Object v = it.next();
							
							if(v instanceof Model) {
								values.append(((Model) v).asCSV()).append(';');
							} else {
								values.append(v).append(';');
							}
						}

					}
				} else if(f.getType() == byte[].class) {
					values.append(new String((byte[]) value)).append(',');
				} else if(f.getType() == float[].class) {
					
					for (float val : (float[]) value)
					{
						values.append(val).append(';');

					}
				

					values.append(',');
					
				} else if(f.getType() == int[].class) {
					

					for (int val : (int[]) value)
					{
						values.append(val).append(';');

					}
				
					values.append(',');
					
				} else if(isModel) {
					values.append(((Model) value).asCSV()).append(',');
					
				} else {
					values.append(value).append(',');
				}
			} catch (IllegalArgumentException e) {
				Log.e(LOG, "error making CSV", e);

			} catch (IllegalAccessException e) {
				Log.e(LOG, "error making CSV", e);

			} catch (JSONException e) {
				Log.e(LOG, "error making CSV", e);
			} catch (NullPointerException e) {
				Log.e(LOG, "error making CSV", e);

			}

		}

		return header.toString() + '\n' + values.toString() + '\n';
	}


}
