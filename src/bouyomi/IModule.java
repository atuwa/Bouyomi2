package bouyomi;

import net.dv8tion.jda.api.events.GenericEvent;

public interface IModule{
	public default void precall(Tag t) {}
	public void call(Tag tag);
	public default void postcall(Tag t) {}
	public default void event(BouyomiEvent o) {}
	/**@see net.dv8tion.jda.api.hooks.ListenerAdapter*/
	public default void onGenericEvent(GenericEvent event) {}
	public static interface BouyomiEvent{}
}