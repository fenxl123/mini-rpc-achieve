package com.lfx.rpc.common;

import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 使用hessian序列化与反序列化数据
 *
 */
public class HessianUtil {
    private HessianUtil() {
    	
    }
    /**
     *序列化数据
     * @throws IOException 
     */
    @SuppressWarnings("unchecked")
    public static <T> byte[] serialize(T obj) throws IOException {
        Class<T> cls = (Class<T>) obj.getClass();
        ByteArrayOutputStream os=null;
        HessianOutput ho=null;
        try{
	        os = new ByteArrayOutputStream();  
	        ho = new HessianOutput(os);  
	        ho.writeObject(obj);  
	        return os.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        } finally {
           os.close();
        }
       
       
    }

    /**
     *反序列化数据
     */
    public static <T> T deserialize(byte[] data) {
        try {
        	 ByteArrayInputStream is = new ByteArrayInputStream(data);  
        	 HessianInput hi = new  HessianInput(is);  
        	 return (T) hi.readObject();  
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
