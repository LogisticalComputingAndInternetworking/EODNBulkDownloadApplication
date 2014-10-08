package bulkdownloadapplication;

import java.awt.Component;
import java.awt.GraphicsConfiguration;
import java.awt.Shape;
import java.awt.Window;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class AWTUtilitiesWrapper 
{
    private static Class<?> awtUtilitiesClass;
    private static Class<?> translucencyClass;
    private static Method mIsTranslucencySupported,  mIsTranslucencyCapable,  mSetWindowShape,  mSetWindowOpacity,  mSetWindowOpaque;
    public static Object PERPIXEL_TRANSPARENT,  TRANSLUCENT,  PERPIXEL_TRANSLUCENT;

    static void init() 
    {
        try 
        {
            awtUtilitiesClass = Class.forName("com.sun.awt.AWTUtilities");
            translucencyClass = Class.forName("com.sun.awt.AWTUtilities$Translucency");
            
            if (translucencyClass.isEnum()) 
            {
                Object[] kinds = translucencyClass.getEnumConstants();
                if (kinds != null) 
                {
                    PERPIXEL_TRANSPARENT = kinds[0];
                    TRANSLUCENT = kinds[1];
                    PERPIXEL_TRANSLUCENT = kinds[2];
                }
            }
            mIsTranslucencySupported = awtUtilitiesClass.getMethod("isTranslucencySupported", translucencyClass);
            mIsTranslucencyCapable = awtUtilitiesClass.getMethod("isTranslucencyCapable", GraphicsConfiguration.class);
            mSetWindowShape = awtUtilitiesClass.getMethod("setWindowShape", Window.class, Shape.class);
            mSetWindowOpacity = awtUtilitiesClass.getMethod("setWindowOpacity", Window.class, float.class);
            mSetWindowOpaque = awtUtilitiesClass.getMethod("setWindowOpaque", Window.class, boolean.class);
        } 
        catch (NoSuchMethodException ex) 
        {
        } 
        catch (SecurityException ex) 
        {
        } 
        catch (ClassNotFoundException ex) 
        {
        }
    }

    static 
    {
        init();
    }
    
    private static boolean isSupported(Method method, Object kind) 
    {
        if (awtUtilitiesClass == null || method == null)
        {
            return false;
        }
        try 
        {
            Object ret = method.invoke(null, kind);
            if (ret instanceof Boolean) 
            {
                return ((Boolean)ret).booleanValue();
            }
        } 
        catch (IllegalAccessException ex) 
        {
        } 
        catch (IllegalArgumentException ex) 
        {
        } 
        catch (InvocationTargetException ex) 
        {
        }
        return false;
    }
    
    public static boolean isTranslucencySupported(Object kind) 
    {
        if (translucencyClass == null) 
        {
            return false;
        }
        return isSupported(mIsTranslucencySupported, kind);
    }
    
    public static boolean isTranslucencyCapable(GraphicsConfiguration gc) 
    {
        return isSupported(mIsTranslucencyCapable, gc);
    }
    
    private static void set(Method method, Component window, Object value) 
    {
        if (awtUtilitiesClass == null || method == null)
        {
            return;
        }
        try 
        {
            method.invoke(null, window, value);
        } 
        catch (IllegalAccessException ex) 
        {
        } 
        catch (IllegalArgumentException ex) 
        {
        } 
        catch (InvocationTargetException ex) 
        {
        }
    }
    
    public static void setWindowShape(Component window, Shape shape) 
    {
        set(mSetWindowShape, window, shape);
    }

    public static void setWindowOpacity(Component window, float opacity) 
    {
        set(mSetWindowOpacity, window, Float.valueOf(opacity));
    }
    
    public static void setWindowOpaque(Component window, boolean opaque) 
    {
        set(mSetWindowOpaque, window, Boolean.valueOf(opaque));
    }
}
