package bouyomi;

import bouyomi.DiscordBOT.BouyomiBOTConection;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

public class DiscordAPI{
	public static boolean chatDefaultHost(Tag tag,String string){
		return chatDefaultHost(tag.con,string);
	}
	public static boolean chatDefaultHost(BouyomiConection con,String string){
		if(DiscordBOT.DefaultHost==null)return false;
		if(con instanceof BouyomiBOTConection) {
			BouyomiBOTConection botc=(BouyomiBOTConection)con;
			DiscordBOT.DefaultHost.log("「"+botc.server.getName()+"」の「"+botc.event.getTextChannel().getName()+"」で\n```\n"+string+"\n```");
		}
		return DiscordBOT.DefaultHost.send(con,string);
	}
	public static boolean chatDefaultHost(String gid,String cid,String c){
		if(DiscordBOT.DefaultHost==null)return false;
		Guild g=DiscordBOT.DefaultHost.jda.getGuildById(gid);
		TextChannel ch=DiscordBOT.DefaultHost.jda.getTextChannelById(cid);
		DiscordBOT.DefaultHost.log("「"+g.getName()+"」の「"+ch.getName()+"」で\n```\n"+c+"\n```");
		return DiscordBOT.DefaultHost.send(gid,cid,c);
	}
}