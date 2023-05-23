package Generic;


import Annotation.TipoChave;
import Dao.ClienteDao;
import Domain.Persists;
import Singleton.SingletonMap;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class GenericDAO<T extends Persists, E extends Serializable> implements IGenericDAO<T, E> {
    private SingletonMap singletonMap;
    protected GenericDAO(){this.singletonMap = SingletonMap.getInstance();}

    public abstract Class<T> getClassType();

    //public abstract void alterar(T entity, T entityCadastrado);

    public E getChave(T entity){
        Field[] fields = entity.getClass().getDeclaredFields();
        E returnValue = null;
        for (Field field: fields){
            if (field.isAnnotationPresent(TipoChave.class)){
                TipoChave tipoChave = field.getAnnotation(TipoChave.class);
                String nomeMethod = tipoChave.value();
                try {
                    Method method = entity.getClass().getMethod(nomeMethod);
                    returnValue= (E) method.invoke(entity);
                    return returnValue;
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }

            }

        }
        if (returnValue == null){
            throw  new RuntimeException("não encontrado");
        }
        return null;
    }

    @Override
    public boolean cadastrar(T entity) {
        Map<E, T> mapIner = getMap();
        E chave = getChave(entity);
        if (mapIner.containsKey(chave)) {
            return false;
        }
        mapIner.put(chave, entity);
        return true;
    }

    private Map<E, T> getMap(){
        Map<E, T> mapIner = (Map<E, T>) this.singletonMap.getMap().get(getClassType());
        if (mapIner == null) {
            mapIner = new HashMap<>();
            this.singletonMap.getMap().put(getClassType(), mapIner);
        }
        return mapIner;
    }

    @Override
    public boolean excluir(E valor) {
        Map<E, T> mapIner = getMap();
        T objetoCadastrado = mapIner.get(valor);
        if (objetoCadastrado!=null){
            mapIner.remove(valor, objetoCadastrado);
            return true;
        }
        return false;

    }

    @Override
    public boolean alterar(T entity) {
        Map<E, T> mapIner = getMap();
        E chave = getChave(entity);
        T objetoCadastrado = mapIner.get(chave);
        if (objetoCadastrado != null) {
            mapIner.replace(chave,entity);
            return true;
            //alterar(entity, objetoCadastrado);
        }
        return false;
    }

    @Override
    public T consultar(E valor) {
        Map<E, T> mapIner = getMap();
        return mapIner.get(valor);
    }
}
