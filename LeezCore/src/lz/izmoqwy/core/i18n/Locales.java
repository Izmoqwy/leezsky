package lz.izmoqwy.core.i18n;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum Locales {

	FRENCH("fr"), ENGLISH("en");

	private final String langCode;

	@Override
	public String toString() {
		return this.langCode;
	}

}
