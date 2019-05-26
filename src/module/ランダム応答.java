package module;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Random;

import bouyomi.BouyomiProxy;
import bouyomi.DailyUpdate;
import bouyomi.DailyUpdate.IDailyUpdate;
import bouyomi.DiscordAPI;
import bouyomi.IAutoSave;
import bouyomi.IModule;
import bouyomi.Tag;

public class ランダム応答 implements IModule, IDailyUpdate,IAutoSave{

	private int rand;
	private ArrayList<String> list=new ArrayList<String>();
	public ランダム応答() {
		String s=BouyomiProxy.Config.get("ランダム応答");
		try{
			rand=Integer.parseInt(s);
		}catch(NumberFormatException nfe) {

		}
		DailyUpdate.Ragister("ランダム応答",this);
	}
	@Override
	public void call(Tag tag){
		String s=tag.getTag("ランダム応答");
		if(s!=null) {
			long seed=s.hashCode()+(long)rand;
			Random r=new Random(seed);
			int index=r.nextInt(list.size());
			String get=list.get(index);
			DiscordAPI.chatDefaultHost(tag,"/"+get);
		}
	}
	@Override
	public void update(){
		rand=new SecureRandom().nextInt();
		BouyomiProxy.Config.put("ランダム応答",Integer.toString(rand));
	}
	@Override
	public void autoSave() throws IOException{
		// TODO 自動生成されたメソッド・スタブ

	}
}