package org.vividus.util;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Hack for updating final static field of external library
 */
public final class FieldUtils
{
    private static final VarHandle MODIFIERS;

    static
    {
        try
        {
            var lookup = MethodHandles.privateLookupIn(Field.class, MethodHandles.lookup());
            MODIFIERS = lookup.findVarHandle(Field.class, "modifiers", int.class);
        }
        catch (IllegalAccessException | NoSuchFieldException e)
        {
            throw new RuntimeException("The 'modifiers' field cannot be handled", e);
        }
    }

    private FieldUtils()
    {
    }

    public static void setFinalStatic(Field field, Object value)
    {
        try
        {
            MODIFIERS.set(field, field.getModifiers() & ~Modifier.FINAL);
            field.setAccessible(true);
            field.set(null, value);
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(String.format("The '%s' field cannot be modified", field.getName()), e);
        }
    }
}
