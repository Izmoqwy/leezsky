package lz.izmoqwy.core.i18n;

public interface i18nLocale {

	void set(String string);

	Locales getDefaultLocale();

	String getSavableMessage();
	String getEnumName();

}
