package com.infinimeme.tilepile.data;

import java.io.File;

import com.infinimeme.tilepile.common.Mural;
import com.infinimeme.tilepile.common.Palette;
import com.infinimeme.tilepile.common.States;
import com.infinimeme.tilepile.common.TilepileObject;

public interface DataConstants {

	public static enum Type {
		MURAL {
			@Override
			public String getKeyPrefix() {
				return "mural";
			}
		},
		PALETTE {
			@Override
			public String getKeyPrefix() {
				return "palette";
			}
		},
		STATES {
			@Override
			public String getKeyPrefix() {
				return "states";
			}
		},
		STATION {
			@Override
			public String getKeyPrefix() {
				return "station";
			}
		},
		MAINSTATION {
			@Override
			public String getKeyPrefix() {
				return "mainStation";
			}
		};

		public abstract String getKeyPrefix();
	}

	public static enum PersistentType {
		MURAL {
			@Override
			public Type getType() {
				return Type.MURAL;
			}

			@Override
			public Class<Mural> getPersistentClass() {
				return Mural.class;
			}
		},
		PALETTE {
			@Override
			public Type getType() {
				return Type.PALETTE;
			}

			@Override
			public Class<Palette> getPersistentClass() {
				return Palette.class;
			}
		},
		STATES {
			@Override
			public Type getType() {
				return Type.STATES;
			}

			@Override
			public Class<States> getPersistentClass() {
				return States.class;
			}
		};

		public abstract Type getType();

		public abstract Class<? extends TilepileObject> getPersistentClass();

	};

	public static final File DATA_DIRECTORY = new File("tilepileData");

	public static final String DATA_FILE_EXTENSION = ".ser.gz";

}
