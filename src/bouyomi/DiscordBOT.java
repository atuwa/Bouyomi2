package bouyomi;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

public class DiscordBOT extends ListenerAdapter{
	public JDA jda;
	public final int id;
	private List<String> whiteListS=new ArrayList<String>();
	private List<String> whiteListC=new ArrayList<String>();
	private List<String> speakListC=new ArrayList<String>();
	private static int lastID;
	private static ExecutorService pool=new ThreadPoolExecutor(1, Integer.MAX_VALUE,60L, TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>());
	private static int threads=0;
	public static ArrayList<DiscordBOT> bots=new ArrayList<DiscordBOT>();
	public Map<String, String> conf=new HashMap<String, String>();
	public static DiscordBOT DefaultHost;
	public DiscordBOT(String token, Map<String, String> map) throws LoginException {
		for(Entry<String, String> m:map.entrySet()) {
			if(m.getValue().equals("active")) {
				whiteListC.add(m.getKey());
			}else if(m.getValue().equals("yomu")) {
				whiteListC.add(m.getKey());
				speakListC.add(m.getKey());
			}else conf.put(m.getKey(),m.getValue());
		}
		try{
			jda=new JDABuilder().setToken(token).addEventListeners(this).build();
		}catch(LoginException e) {
			jda=new JDABuilder(AccountType.CLIENT).setToken(token).addEventListeners(this).build();
		}
		if(DefaultHost==null)DefaultHost=this;
		id=lastID;
		lastID++;
		// optionally block until JDA is ready
		try{
			jda.awaitReady();
		}catch(InterruptedException e){
			e.printStackTrace();
		}
	}
	/**サーバー指定のホワイトリスト*/
	public List<String> whiteListS() {
		return whiteListS;
	}
	/**チャンネル指定のホワイトリスト*/
	public List<String> whiteListC() {
		return whiteListC;
	}
	public void onReady(ReadyEvent event){
		bots.add(this);
		JDA jda=event.getJDA();
		StringBuilder sb=new StringBuilder("API is ready=");
		sb.append(id).append("\tユーザ名=").append(jda.getSelfUser().getName()).append("\n");
		for(Guild g:event.getJDA().getGuilds()){
			sb.append(g.getName()).append("のニックネーム=");
			sb.append(g.getMember(jda.getSelfUser()).getNickname()).append("\n");
		}
		System.out.println(sb);
	}
	@Override
	public void onMessageReceived(MessageReceivedEvent event){
		if(event.isFromType(ChannelType.PRIVATE)){
			System.out.printf("[PM] %s: %s\n",event.getAuthor().getName(),event.getMessage().getContentDisplay());
		}else{
			if(!Boot.loadend)while(!Boot.loadend) {
				try{
					Thread.sleep(100);
				}catch(InterruptedException e){
					e.printStackTrace();
				}
			}
			//test(event);
			if(whiteListS.isEmpty()||whiteListS.contains(event.getGuild().getId())){
				String cid=event.getTextChannel().getId();
				if(whiteListC.contains(cid)){
					threads++;
					if(threads>3)System.err.println("警告：実行中のメッセージスレッドが"+threads+"件です");
					BouyomiBOTConection con=new BouyomiBOTConection(event);
					con.speak=speakListC.contains(cid);
					pool.execute(con);//スレッドプールで実行する
					threads--;
				}else {
					//System.out.println("対象外"+event.getTextChannel().getName());
				}
			}
		}
	}
	public void log(String s) {
		if(BouyomiProxy.log_guild==null||BouyomiProxy.log_channel==null)return;
		Guild g=jda.getGuildById(BouyomiProxy.log_guild);
		TextChannel c=jda.getTextChannelById(BouyomiProxy.log_channel);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日HH時mm分");
		s=sdf.format(new Date())+"\n"+s;
		send(g,c,s);
	}
	public Guild getGuild(String gid) {
		return jda.getGuildById(gid);
	}
	public TextChannel getTextChannel(String cid) {
		return jda.getTextChannelById(cid);
	}
	public static class BouyomiBOTConection extends BouyomiConection{
		public final String userName;
		public final Guild server;
		public final Attachment[] list;
		public final MessageReceivedEvent event;
		public final MessageChannel channel;
		public final TextChannel textChannel;
		public BouyomiBOTConection(MessageReceivedEvent event){
			super.user=event.getMember().getEffectiveName();
			super.userid=event.getMember().getUser().getId();
			super.text=event.getMessage().getContentRaw();
			userName=event.getMember().getUser().getName();
			server=event.getGuild();
			channel=event.getChannel();
			textChannel=event.getTextChannel();
			List<Attachment> as=event.getMessage().getAttachments();
			if(as!=null&&as.size()>0) {
				list=as.toArray(new Attachment[as.size()]);
				StringBuilder sb=new StringBuilder(text);
				for(Attachment a:list)sb.append(" file://").append(a.getFileName());
				text=sb.toString();
			}else list=new Attachment[0];
			this.event=event;
			//System.err.println(text);
		}
	}
	public void setUserName(String name) {
		jda.getSelfUser().getManager().setName(name).queue();
	}
	@SuppressWarnings("unused")
	private void test(MessageReceivedEvent event){
		StringBuilder sb=new StringBuilder(event.getGuild().getName()).append("\t");
		sb.append(event.getGuild().getId()).append("\n");
		sb.append(event.getTextChannel().getName()).append("\t").append(event.getTextChannel().getId()).append("\n");
		String nn=event.getMember().getNickname();
		if(nn==null)nn="ニックネーム無し";
		sb.append(nn).append("\t").append(event.getMember().getUser().getName()).append("\t");
		sb.append(event.getMember().getEffectiveName()).append("\t");
		sb.append(event.getMember().getUser().getId()).append("\n");
		sb.append(event.getMessage().getContentDisplay());
		System.out.println(sb);
		if(event.getTextChannel().getId().equals("539114426310066176")&&event.getMember().getUser().getId().equals("544529530866368522")){
			if(event.getTextChannel().canTalk())System.out.println("test");
			String s=event.getMessage().getContentRaw();
			String p=Pattern.quote(".");
			Matcher m=Pattern.compile("[0-9]{1,3}"+p+"[0-9]{1,3}"+p+"[0-9]{1,3}"+p+"[0-9]{1,3}").matcher(s);
			String msg;
			if(m.find()) {
				msg=m.group();
				if(!msg.equals(s))msg="余計な物が";
			}else msg="IPアドレスじゃない";
			event.getTextChannel().sendMessage(new MessageBuilder().append(msg).build()).queue();
			try{
				BufferedImage image=new BufferedImage(300,70,BufferedImage.TYPE_4BYTE_ABGR);
				Graphics2D g=image.createGraphics();
				g.setColor(Color.white);
				g.fillRect(0,0,300,100);
				g.setFont(new Font(null,Font.BOLD,30));
				g.setColor(Color.black);
				g.drawString(msg,0,40);
				Thread.sleep(200);
				ByteArrayOutputStream os=new ByteArrayOutputStream();
				ImageIO.write(image,"png",os);
				byte[] ba=os.toByteArray();
				event.getTextChannel().sendFile(ba,"res.png").queue();
			}catch(IOException | InterruptedException e){
				e.printStackTrace();
			}
		}
	}
	public boolean chat(BouyomiConection bc,String c){
		return send(bc,c);
	}
	public boolean send(BouyomiConection bc,String c){
		if(bc instanceof BouyomiBOTConection) {
			BouyomiBOTConection botc=(BouyomiBOTConection)bc;
			send(botc.server,botc.event.getTextChannel(),c);
			return true;
		}
		return false;
	}
	public boolean send(String gid,String cid,String c){
		Guild g=jda.getGuildById(gid);
		TextChannel tc=jda.getTextChannelById(cid);
		if(g==null||tc==null)return false;
		send(c,g,tc,(NamedFileObject)null);
		return true;
	}
	public void send(Guild g,TextChannel tc,String c){
		send(c,g,tc,(NamedFileObject)null);
	}
	public void send(String c,Guild g,TextChannel tc,byte[]... bi){
		NamedFileObject[] fo=new NamedFileObject[bi.length];
		for(int i=0;i<bi.length;i++) {
			ByteArrayInputStream is=new ByteArrayInputStream(bi[i]);
			fo[i]=new NamedFileObject(is,"byte"+i);
		}
		send(c,g,tc,fo);
	}
	public void send(String c,Guild g,TextChannel tc,File... in) throws FileNotFoundException{
		NamedFileObject[] fo=new NamedFileObject[in.length];
		for(int i=0;i<in.length;i++) {
			FileInputStream is=new FileInputStream(in[i]);
			fo[i]=new NamedFileObject(is,in[i].getName());
		}
		send(c,g,tc,fo);
	}
	public void send(String c,Guild g,TextChannel tc,InputStream... is){
		NamedFileObject[] fo=new NamedFileObject[is.length];
		for(int i=0;i<is.length;i++) {
			fo[i]=new NamedFileObject(is[i],"stream"+i);
		}
		send(c,g,tc,fo);
	}
	public static class NamedFileObject{
		public InputStream is;
		public String name;
		public NamedFileObject(InputStream is2,String s){
			is=is2;
			name=s;
		}
	}
	public void send(String c,Guild pg,TextChannel ptc,NamedFileObject... fo){
		if(pg==null||ptc==null)return;
		Guild g=jda.getGuildById(pg.getIdLong());
		TextChannel tc=g.getTextChannelById(ptc.getIdLong());
		//System.out.println(g.getMember(jda.getSelfUser()).getEffectiveName()+"<"+c);
		//System.out.println("send="+c);
		MessageAction ma=tc.sendMessage(new MessageBuilder().append(c).build());
		if(fo!=null)for(NamedFileObject i:fo) {
			if(i!=null)ma.addFile(i.is,i.name);
		}
		try{
			ma.queue();
		}catch(Exception e) {
			SimpleDateFormat df = new SimpleDateFormat("yyyy年MM月dd日HH時mm分");
			String date=df.format(new Date());
			System.err.println(date+"送信エラー");
			try{
				FileOutputStream fos=new FileOutputStream("送信エラー.log",true);//追加モードで開く
				BufferedOutputStream bo=new BufferedOutputStream(fos);
				PrintStream ps=new PrintStream(bo,true,StandardCharsets.UTF_8.name());
				try{
					ps.append(date).append('\t');
					e.printStackTrace(ps);
					ps.println();
				}catch(Exception ex) {
					e.printStackTrace();
				}finally {
					try{
						bo.close();
					}catch(IOException ex){
						e.printStackTrace();
					}
				}
			}catch(FileNotFoundException | UnsupportedEncodingException ex){
				e.printStackTrace();
			}
		}
	}
}
