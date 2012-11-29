package com.github.jmkgreen.morphia.mapping.lazy;

import com.thoughtworks.proxy.ProxyFactory;
import com.thoughtworks.proxy.kit.ObjectReference;
import com.thoughtworks.proxy.toys.delegate.DelegationMode;
import com.thoughtworks.proxy.toys.hotswap.HotSwappingInvoker;
import java.lang.reflect.Method;

class NonFinalizingHotSwappingInvoker extends HotSwappingInvoker {

    public NonFinalizingHotSwappingInvoker(Class[] types, ProxyFactory proxyFactory, ObjectReference delegateReference,
                                           DelegationMode delegationMode) {
        super(types, proxyFactory, delegateReference, delegationMode);
    }

    private static final long serialVersionUID = 1L;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("finalize".equals(method.getName()) && args != null && args.length == 0) {
            return null;
        }

        return super.invoke(proxy, method, args);
    }

}
