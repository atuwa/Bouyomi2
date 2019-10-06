package bouyomi;

import static bouyomi.BouyomiProxy.*;

import java.util.Map.Entry;

import bouyomi.Counter.CountData;
import bouyomi.DiscordBOT.BouyomiBOTConection;
import bouyomi.DiscordBOT.DiscordAPI;
import bouyomi.Util.Pass;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

public class Tag{
	public BouyomiConection con;
	public boolean isTagTrim=true;
	public Tag(BouyomiConection bc) {
		con=bc;
	}
	public Guild getGuild() {
		if(con instanceof BouyomiBOTConection) {
			return ((BouyomiBOTConection)con).server;
		}
		return null;
	}
	public MessageChannel getChannel(){//botc.event.getTextChannel()
		if(con instanceof BouyomiBOTConection) {
			return ((BouyomiBOTConection)con).event.getChannel();
		}
		return null;
	}
	/**自作プロキシの追加機能*/
	public void call() {
		if(module!=null)module.precall(this);
		if(con.text.equals("!help")) {
			chatDefaultHost("説明(仮) https://github.com/atuwa/Bouyomi/wiki");
			con.text="";
			return;
		}
		if(module!=null)module.call(this);
		Counter.count(this);
		String tag=getTag("強制終了");
		if(tag!=null) {
			Pass.exit(tag);
		}
		tag=getTag("ユーザID","ユーザＩＤ","ユーザーＩＤ","ユーザーID");
		if(tag!=null) {
			if(!tag.isEmpty()) {
				String id=getUserID(tag);
				System.out.println("ID取得「"+tag+"」のID="+id);
				if(con.mute);
				else if(id==null)chatDefaultHost("取得失敗");
				else chatDefaultHost("/"+id);
			}else {
				System.out.println("ID取得「"+con.user+"」のID="+con.userid);
				if(con.mute);
				else if(con.userid==null)chatDefaultHost("取得失敗");
				else chatDefaultHost("/"+con.userid);
			}
		}
		if(isAdmin()) {
			tag=getTag("BOTユーザ名","botユーザ名","BOTNAME","botname");
			if(tag!=null) {
				int tab=tag.indexOf(',');
				if(tab<0||tab+1>tag.length()) {
					int id=Integer.parseInt(tag);
					String s;
					if(DiscordBOT.bots.size()>id&&id>=0) {
						DiscordBOT b=DiscordBOT.bots.get(id);
						String name=b.jda.getSelfUser().getName();
						s="Bot"+id+"の名前は"+name+"です";
					}else s="指定されたIDのBotが存在しません("+id+")";
					System.out.println(s);
					chatDefaultHost(s);
				}else {
					String key=tag.substring(0,tab);
					String val=tag.substring(tab+1);
					int id=Integer.parseInt(key);
					String s;
					if(DiscordBOT.bots.size()>id&&id>=0) {
						DiscordBOT b=DiscordBOT.bots.get(id);
						b.setUserName(val);
						s="Bot"+id+"の名前を"+val+"に設定";
					}else s="指定されたIDのBotが存在しません("+id+")";
					System.out.println(s);
					chatDefaultHost(s);
				}
			}
			tag=getTag("BOTNICK","botnick");
			if(tag!=null&&this.con instanceof BouyomiBOTConection) {
				BouyomiBOTConection botcon=(BouyomiBOTConection)con;
				int tab=tag.indexOf(',');
				if(tab<0||tab+1>tag.length()) {
					int id=Integer.parseInt(tag);
					String s;
					if(DiscordBOT.bots.size()>id&&id>=0) {
						DiscordBOT b=DiscordBOT.bots.get(id);
						String name=botcon.server.getMember(b.jda.getSelfUser()).getNickname();
						s="Bot"+id+"のニックネームは"+name+"です";
					}else s="指定されたIDのBotが存在しません("+id+")";
					System.out.println(s);
					chatDefaultHost(s);
				}else {
					String key=tag.substring(0,tab);
					String val=tag.substring(tab+1);
					int id=Integer.parseInt(key);
					String s;
					if(DiscordBOT.bots.size()>id&&id>=0) {
						DiscordBOT b=DiscordBOT.bots.get(id);
						try {
							Member bot=botcon.server.getMember(b.jda.getSelfUser());
							botcon.server.getController().setNickname(bot,val).queue();
							s="Bot"+id+"のニックネームを"+val+"に設定";
						}catch(InsufficientPermissionException pe) {
							s="Bot"+id+"はニックネームを変更する権限を持っていません";
						}
					}else s="指定されたIDのBotが存在しません("+id+")";
					System.out.println(s);
					chatDefaultHost(s);
				}
			}
		}
		if(module!=null)module.postcall(this);
	}
	public boolean chatDefaultHost(String s){
		return DiscordAPI.chatDefaultHost(con,s);
	}
	public String getTag(String... key) {
		for(String s:key) {
			String t=getTag(s);
			if(t!=null)return t;
		}
		return null;
	}
	/**タグ取得*/
	public String getTag(String key) {
		TagCommand tc=getTagCommand(key);
		if(tc==null)return null;
		tc.removeTag();
		if(isTagTrim)return tc.toString();
		return tc.value;
	}
	public static class TagCommand{
		public String key;
		public String value;
		public Tag tag;
		public TagCommand(Tag tag2, String key2,String val){
			key=key2;
			value=val;
			tag=tag2;
		}
		public void removeTag() {
			tag.removeTag(key,value);
		}
		public void replaceTag(String s) {
			tag.replaceTag(key,value,s);
		}
		@Override
		public String toString() {
			return value.trim();
		}
	}
	public TagCommand getTagCommand(String... key) {
		for(String s:key) {
			TagCommand t=getTagCommand(s);
			if(t!=null)return t;
		}
		return null;
	}
	public TagCommand getTagCommand(String key) {
		if(con.text.length()<1)return null;
		if(con.text.equals(key)) {
			//DiscordAPI.chatDefaultHost(key+"タグを検出しました");
			return new TagCommand(this,key,"");
		}
		int index=con.text.indexOf(key+"(");
		if(index<0)index=con.text.indexOf(key+"（");
		if(index<0)return null;//タグを含まない時
		int ki=con.text.indexOf(')');//半角
		int zi=con.text.indexOf('）');//全角
		if(ki<0)ki=zi;
		if(ki<0)return null;//閉じカッコが無い時
		if(ki<index+key.length()+1)return null;//閉じカッコの位置がおかしい時
		if(ki==index+key.length()+1) {
			//DiscordAPI.chatDefaultHost(key+"タグを検出しました");
			return new TagCommand(this, key,"");//0文字
		}
		String tag=con.text.substring(index+key.length()+1,ki);
		//System.out.println("タグ取得k="+key+"v="+tag);
		//DiscordAPI.chatDefaultHost(key+"タグを検出しました");
		return new TagCommand(this, key,tag);
	}
	public void removeTag(String tagName,String val) {
		replaceTag(tagName,val,null);
	}
	public void replaceTag(String tagName,String val,String replace){
		//System.out.println("元データ　"+con.text);
		StringBuilder sb0=new StringBuilder(tagName);
		sb0.append("(").append(val);//これ半角しか削除できない
		String remove=sb0.toString();
		int index=con.text.indexOf(remove);
		//System.out.println("タグ消去　"+remove+"&index="+index);
		if(index<0) {
			StringBuilder sb1=new StringBuilder(tagName);
			sb1.append("（").append(val);//こっちで全角のカッコを処理
			remove=sb1.toString();
			index=con.text.indexOf(remove);
			//System.out.println("タグ消去　"+remove+"&index="+index);
			if(index<0)return;
		}
		StringBuilder sb=new StringBuilder();
		if(index>0)sb.append(con.text.substring(0,index));//タグで始まる時以外
		if(replace!=null)sb.append(replace);
		if(con.text.length()>index+remove.length())sb.append(con.text.substring(index+remove.length()+1));
		con.text=sb.toString();
		//System.out.println("タグ消去結果　"+con.text);
	}
	public boolean isAdmin(){
		if(BouyomiProxy.admin==null)return false;
		return BouyomiProxy.admin.isAdmin(con.userid);
	}
	public String getUserNick(String id) {
		if(con instanceof BouyomiBOTConection) {
			return ((BouyomiBOTConection)con).bot.getNick(((BouyomiBOTConection)con).server.getId(),id);
		}
		return getUserName(id);
	}
	public String getUserName(String id) {
		if(con instanceof BouyomiBOTConection) {
			return ((BouyomiBOTConection)con).bot.getName(id);
		}
		CountData cd=Counter.usercount.get(id);
		if(cd==null)return null;
		return cd.name;
	}
	public String getUserID(String id) {
		if(con instanceof BouyomiBOTConection) {
			return ((BouyomiBOTConection)con).server.getMemberById(id).getUser().getId();
		}
		for(Entry<String, CountData> e:Counter.usercount.entrySet()) {
			if(id.equals(e.getValue().name))return e.getKey();
		}
		return null;
	}
}
