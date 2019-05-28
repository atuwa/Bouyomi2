package bouyomi;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.login.LoginException;

public class Boot{
	private static int loadBots;
	public static boolean loadend;
	public static void load(String path) throws IOException {
		HashMap<String,String> map=new HashMap<String,String>();
		FileInputStream fos=new FileInputStream(path);
		InputStreamReader isr=new InputStreamReader(fos,StandardCharsets.UTF_8);
		BufferedReader br=new BufferedReader(isr);
		try {
			while(true) {
				String line=br.readLine();
				if(line==null)break;
				if(line.length()>0&&line.charAt(0)=='#')continue;
				int tab=line.indexOf('\t');
				if(tab<0||tab+1>line.length()) {
					map.put(line,"");//タブがない時ORフォーマットがおかしいときは行をキーにして値を0文字に
				}
				System.out.println(line);
				String key=line.substring(0,tab);
				String val=line.substring(tab+1);
				if(key.equals("token")) {
					map.put(key,val);
					loadBOT(map);
					map.clear();
				}else if(key.equals("log_guild")) {
					BouyomiProxy.log_guild=val;
					continue;
				}else if(key.equals("log_channel")) {
					BouyomiProxy.log_channel=val;
					continue;
				}
				map.put(key,val);
			}
			if(map.containsKey("token"))loadBOT(map);
		}catch(FileNotFoundException fnf){

		}finally {
			br.close();
		}
		while(true) {
			try{
				if(loadBots<=DiscordBOT.bots.size())break;
				Thread.sleep(50);
			}catch(InterruptedException e){
				e.printStackTrace();
			}
		}
		loadend=true;
	}
	public static void loadBOT(Map<String,String> map) {
		String token=map.get("token");
		try{
			loadBots++;
			new DiscordBOT(token,map);
		}catch(LoginException e){
			e.printStackTrace();
			loadBots--;
		}
	}
}