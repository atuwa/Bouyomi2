package bouyomi;

public class DiscordAPI{
	public static boolean chatDefaultHost(Tag tag,String string){
		return chatDefaultHost(tag.con,string);
	}
	public static boolean chatDefaultHost(BouyomiConection con,String string){
		DiscordBOT.DefaultHost.log(string);
		return DiscordBOT.DefaultHost.send(con,string);
	}
	public static boolean chatDefaultHost(String gid,String cid,String c){
		DiscordBOT.DefaultHost.log(c);
		return DiscordBOT.DefaultHost.send(gid,cid,c);
	}
}