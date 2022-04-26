package org.ssi.factory;

public interface ObjectFactory<T>
{
    T newInstance();
}