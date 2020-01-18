package lz.izmoqwy.core;

public interface FireAction {

	interface Player {

		void fire(Player player);

	}

	interface Boolean {

		void fire(boolean bool);

	}

	interface String {

		void fire(String string);

	}

	interface Int {

		void fire(int number);

	}

	interface Double {

		void fire(double number);

	}

	interface ObjToStr<T> {

		java.lang.String fire(T obj);

	}

}
