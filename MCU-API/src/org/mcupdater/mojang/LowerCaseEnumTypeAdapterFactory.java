package org.mcupdater.mojang;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class LowerCaseEnumTypeAdapterFactory implements TypeAdapterFactory {

	@Override
	public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
		Class<? super T> rawType = type.getRawType();
		if (!rawType.isEnum()) {
			return null;
		}

		final Map<Object, Object> lowercaseToConstant = new HashMap<Object, Object>();
		for (Object constant : rawType.getEnumConstants()) {
			lowercaseToConstant.put(toLowercase(constant), constant);
		}

		return new TypeAdapter<T>() {
			public void write(JsonWriter out, Object value) throws IOException {
				if (value == null)
					out.nullValue();
				else
					out.value((String) LowerCaseEnumTypeAdapterFactory.this.toLowercase(value));
			}

			@SuppressWarnings("unchecked")
			public T read(JsonReader reader) throws IOException
			{
				if (reader.peek() == JsonToken.NULL) {
					reader.nextNull();
					return null;
				}
				return (T) lowercaseToConstant.get(reader.nextString());
			}
		};
	}

	private Object toLowercase(Object o) {
		return o.toString().toLowerCase(Locale.US);
	}

}
