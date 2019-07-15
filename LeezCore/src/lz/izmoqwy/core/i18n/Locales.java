package lz.izmoqwy.core.i18n;

public enum Locales {

	FRENCH("fr"), ENGLISH("en");

	final String langCode;

	Locales(String langCode) {
		this.langCode = langCode;
	}

	@Override
	public String toString() {
		return this.langCode;
	}
}
