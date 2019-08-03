package bouyomi;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class Util{
	public static String IDtoMention(String id) {
		return new StringBuilder("<@!").append(id).append("> ").toString();
	}
	public static boolean chatException(Tag tag,String base,Exception e) {
		StringWriter sw=new StringWriter();
		if(base!=null)sw.write(base);
		PrintWriter pw=new PrintWriter(sw);
		e.printStackTrace(pw);
		pw.close();
		return tag.chatDefaultHost(sw.toString());
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
	public static class Admin{

		private ArrayList<String> list=new ArrayList<String>();

		public Admin() {
			try{
				BouyomiProxy.load(list,"admin.txt");
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		public boolean isAdmin(String id) {
			if(id==null||id.isEmpty())return false;
			return list.contains(id);
		}
	}
	public static class SaveProxyData implements IAutoSave{

		private String file;
		private String base_file;
		private BufferedOutputStream logFileOS;
		public SaveProxyData(String logFile) {
			base_file=logFile;
			file=base_file;
			try{
				FileOutputStream fos=new FileOutputStream(logFile,true);//追加モードでファイルを開く
				logFileOS=new BufferedOutputStream(fos);
			}catch(FileNotFoundException e){
				e.printStackTrace();
			}
			IAutoSave.Register(this);
		}
		public String getFile() {
			return file;
		}
		public void changeFile(String s) throws IOException{
			if(logFileOS!=null) try{
				logFileOS.close();
			}catch(IOException e1){
				e1.printStackTrace();
			}
			Files.move(Paths.get(file),Paths.get(s));
			file=s;
			try{
				FileOutputStream fos=new FileOutputStream(s,true);//追加モードでファイルを開く
				logFileOS=new BufferedOutputStream(fos);
			}catch(FileNotFoundException e){
				e.printStackTrace();
			}
		}
		public void nextFile() {
			int i=1;
			while(true) {
				File f=new File(insert(String.valueOf(i)));
				if(!f.exists())break;
				i++;
			}
			try{
				changeFile(insert(String.valueOf(i)));
			}catch(IOException e){
				e.printStackTrace();
			}
		}
		private String insert(String s) {
			int index=base_file.lastIndexOf('.');
			if(index>0&&index<base_file.length())return base_file.substring(0,index)+s+base_file.substring(index);
			return "";
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
	/**ワンタイムパスワード機能*/
	public static class Pass{
		private static ArrayList<String> pass=new ArrayList<String>();
		public static void read() throws IOException {
			FileInputStream fis=new FileInputStream("pass.txt");
			InputStreamReader isr=new InputStreamReader(fis,StandardCharsets.UTF_8);
			BufferedReader br=null;
			try {
				br=new BufferedReader(isr);
				while(br.ready()) {
					String line=br.readLine();
					if(line==null)break;
					if(!line.isEmpty())pass.add(line);
				}
			}finally {
				if(br!=null)br.close();
				isr.close();
				fis.close();
			}
		}
		public static void write() throws IOException {
			FileOutputStream fos=new FileOutputStream("pass.txt");
			OutputStreamWriter osw=new OutputStreamWriter(fos,StandardCharsets.UTF_8);
			try{
				for(int i=0;i<pass.size();i++) {
					osw.write(pass.get(i)+"\n");
				}
			}finally{
				osw.close();
			}
		}
		public static void addPass() {
			StringBuilder sb=new StringBuilder("M");
			SecureRandom r=new SecureRandom();
			for(int i=0;i<50;i++) {
				char c=(char) (r.nextInt(81)+42);
				switch(c) {
					case '[':
					case ']':
					case '\\':
					case '<':
					case '>':
					case '.':
					case ',':
					case '^':
					case '/':
					case '@':
					case '?':
					case '*':
					case 96:
						i--;
						continue;
				}
				sb.append(c);
			}
			pass.add(sb.toString());
			try{
				write();
			}catch(IOException e){
				e.printStackTrace();
			}
		}
		public static void exit(String pass) {
			if(pass==null||!Pass.pass.contains(pass))return;
			Pass.pass.remove(pass);
			System.out.println("ワンタイムパスワードで強制終了しますpass="+pass);
			try{
				write();
			}catch(IOException e){
				e.printStackTrace();
			}
			System.exit(1000);
		}
	}
}