/**
 * 
 */
package com.infinimeme.tilepile.data;

import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import com.infinimeme.tilepile.common.TilepileObject;
import com.infinimeme.tilepile.common.TilepileUtils;

/**
 * @author greg
 *
 */
public class DataUtils implements DataConstants {

     public static final <T extends TilepileObject> T readFile(File f, Class<T> clazz) throws IOException, ClassNotFoundException {
        
        ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream(new FileInputStream(f)));
        T object = clazz.cast(ois.readObject());
        ois.close();
        
        return object;
    }
    
    public static final void save(Type type, TilepileObject object) {

        try {

            ObjectOutputStream oos = new ObjectOutputStream(
                new GZIPOutputStream(
                    new FileOutputStream(
                        getFile(type, object.getName())
                    )
                )
            );

            oos.writeObject(object);
            oos.flush();
            oos.close();

        } catch (IOException ioe) {
            TilepileUtils.exceptionReport(ioe);
        }
    }
    
    public static final boolean delete(Type type, String name) {
        return getFile(type, name).delete();
    }
    
    private static final File getDirectory(Type type) {
        return new File(DATA_DIRECTORY, type.getKeyPrefix());
    }
    
    private static final File getFile(Type type, String name) {
        return new File(
            getDirectory(type),
            name + DATA_FILE_EXTENSION
        );
    }
}
