package module;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;

import bouyomi.BouyomiProxy;
import bouyomi.DiscordAPI;
import bouyomi.DiscordBOT;
import bouyomi.DiscordBOT.BouyomiBOTConection;
import bouyomi.DiscordBOT.NamedFileObject;
import bouyomi.IModule;
import bouyomi.Tag;

public class お題抽選システム implements IModule{

	@Override
	public void call(Tag tag){
		if(tag.con instanceof BouyomiBOTConection);
		else return;
		BouyomiBOTConection bc=(BouyomiBOTConection)tag.con;
		if(!bc.channel.getId().equals("574171237861949450"))return;
		String text=tag.con.text.trim();
		if(text.indexOf("【お題】")==0||text.indexOf("【希望お題】")==0){
			int index=text.indexOf('】');
			if(text.length()>index) {
				text=text.substring(index+1).trim();
				お題リストに書き出し(text);
				DiscordAPI.chatDefaultHost(tag.con,"お題候補に「"+text+"」を追加");
			}
		}
		String ts=tag.getTag("お題リストから削除");
		if(ts!=null&&!ts.isEmpty()) {
			synchronized(this) {
				try{
					ArrayList<String> list=new ArrayList<String>();
					BouyomiProxy.load(list,"お題抽選システム-お題リスト.txt");
					if(list.remove(ts)) {
						BouyomiProxy.save(list,"お題抽選システム-お題リスト.txt");
						DiscordAPI.chatDefaultHost(tag.con,"削除成功");
					}else DiscordAPI.chatDefaultHost(tag.con,"削除失敗(存在しません)");
				}catch(IOException e){
					e.printStackTrace();
					DiscordAPI.chatDefaultHost(tag.con,"削除失敗(ファイル操作失敗)");
				}
			}
		}
		ts=tag.getTag("お題リスト全消去");
		if(ts!=null) {
			File f=new File("お題抽選システム-お題リスト.txt");
			try{
				NamedFileObject fo=new NamedFileObject(new FileInputStream(f),"odai.txt");
				DiscordBOT.DefaultHost.send("残っていたお題リスト",bc.server,bc.textChannel,fo);
			}catch(FileNotFoundException e){
				e.printStackTrace();
			}
			f.delete();
		}
		ts=tag.getTag("お題リスト取得");
		if(ts!=null) {
			File f=new File("お題抽選システム-お題リスト.txt");
			try{
				NamedFileObject fo=new NamedFileObject(new FileInputStream(f),"odai.txt");
				DiscordBOT.DefaultHost.send("お題リスト",bc.server,bc.textChannel,fo);
			}catch(FileNotFoundException e){
				e.printStackTrace();
			}
		}
		ts=tag.getTag("お題抽選");
		if(ts!=null) {
			try{
				ArrayList<String> list=new ArrayList<String>();
				BouyomiProxy.load(list,"お題抽選システム-お題リスト.txt");
				SecureRandom rand=new SecureRandom();
				int index=rand.nextInt(list.size());
				ts=list.get(index);
				if(list.remove(index)!=null) {
					BouyomiProxy.save(list,"お題抽選システム-お題リスト.txt");
					DiscordAPI.chatDefaultHost(tag.con,"抽選結果："+ts);
				}else DiscordAPI.chatDefaultHost(tag.con,"削除失敗(存在しません)");
			}catch(IOException e){
				e.printStackTrace();
				DiscordAPI.chatDefaultHost(tag.con,"抽選失敗(ファイル操作失敗)");
			}
		}
	}
	private synchronized void お題リストに書き出し(String 追加分) {
		try{
			FileOutputStream fos=new FileOutputStream("お題抽選システム-お題リスト.txt",true);//追加モードで開く
			BufferedOutputStream 出力先=new BufferedOutputStream(fos);
			try{
				出力先.write((追加分+"\n").getBytes(StandardCharsets.UTF_8));
			}catch(Exception e) {
				e.printStackTrace();
			}finally {
				try{
					出力先.close();
				}catch(IOException e){
					e.printStackTrace();
				}
			}
		}catch(FileNotFoundException e){
			e.printStackTrace();
		}
	}
}