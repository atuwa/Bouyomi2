package bouyomi;

public interface IModule{
	public default void precall(Tag t) {}
	public void call(Tag tag);
	public default void postcall(Tag t) {}
	public default void event(BouyomiEvent o) {}
	public static interface BouyomiEvent{}
}