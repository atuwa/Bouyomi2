package bouyomi;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class Util{
	public static String IDtoMention(String id) {
		return new StringBuilder("<@!").append(id).append("> ").toString();
	}
	/**<a href="https://qiita.com/oyahiroki/items/006b3511fc4136d02ad1">ここから持ってきた</a>*/
	public static class JsonUtil{
	    public static Object[] getAsArray(String json, String code) {
	        Object obj = get(json, code);
	        if (obj instanceof Object[]) {
	            return (Object[]) obj;
	        }if (obj instanceof Map) {
                java.util.Map<?,?> map = (java.util.Map<?,?>) obj;
                Set<?> entrySet = map.entrySet();
                Object[] arr = new Object[entrySet.size()];
                int n = 0;
                for (Object objValue : map.values()) {
                    if (objValue instanceof String) {
                        String sValue = (String) objValue;
                        arr[n] = sValue;
                    } else {
                        arr[n] = objValue;//map.get(obj);
                    }
                    n++;
                }
                return arr;
	        } else {
	            return null;
	        }
	    }
	    public static Object get(String json, String code) {
	        // Get the JavaScript engine
	        ScriptEngineManager manager = new ScriptEngineManager();
	        ScriptEngine engine = manager.getEngineByName("JavaScript");
	        String script = "var obj = " + json + ";";
	        try {
	            engine.eval(script);
	            {
	                Object obj = engine.eval("obj." + code);
	                if (obj instanceof Map) {
	                    java.util.Map<?,?> map = (java.util.Map<?,?>) obj;
	                    Set<?> entrySet = map.entrySet();
	                    Object[] arr = new Object[entrySet.size()];
	                    int n = 0;
	                    for (Object objValue : map.values()) {
	                        if (objValue instanceof String) {
	                            String sValue = (String) objValue;
	                            arr[n] = sValue;
	                        } else {
	                            arr[n] = objValue;//map.get(obj);
	                        }
	                        n++;
	                    }
	                    return arr;
	                }
	                return obj;
	            }
	        } catch (ScriptException e) {
	            e.printStackTrace();
	            return null;
	        }
	    }
	}
	public static class SaveProxyData implements IAutoSave{

		public final String file;
		private BufferedOutputStream logFileOS;
		public SaveProxyData(String logFile) {
			file=logFile;
			try{
				FileOutputStream fos=new FileOutputStream(logFile,true);//追加モードでファイルを開く
				logFileOS=new BufferedOutputStream(fos);
			}catch(FileNotFoundException e){
				e.printStackTrace();
			}
			IAutoSave.Register(this);
		}
		public void log(String s) {
			try{
				logFileOS.write((s.replace('\n',' ')+"\n").getBytes(StandardCharsets.UTF_8));//改行文字を追加してバイナリ化
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		@Override
		public void shutdownHook() {
			try{
				if(logFileOS!=null)logFileOS.close();
			}catch(IOException e){
				e.printStackTrace();
			}
		}
		@Override
		public void autoSave() throws IOException{
			if(logFileOS!=null) {
				logFileOS.flush();
				//System.out.println("定期ログフラッシュ");
			}
		}
	}
}