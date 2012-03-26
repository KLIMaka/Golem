package golem.typesystem;

import gnu.bytecode.Method;
import gnu.bytecode.Type;

import java.util.List;

import ch.lambdaj.Lambda;

public class StaticFunctionTypeResolver implements IFunctionTypeResolver {

    private Methods m_methods;

    public StaticFunctionTypeResolver(Methods meths) {
        m_methods = meths;
    }

    @Override
    public Type get() {
        return null;
    }

    @Override
    public String getName() {
        return "static function " + m_methods.getClazz().getName() + "." + m_methods.getName();
    }

    @Override
    public IMethodResolver match(final List<ITypeResolver> types) {

        return new IMethodResolver() {
            private Method m_method;

            @Override
            public ITypeResolver type() {
                return new ITypeResolver() {
                    public Type get() {
                        if (m_method == null)
                            get_();
                        return m_method.getReturnType();
                    }
                };
            }

            protected Method get_() {
                return get();
            }

            @Override
            public Method get() {
                if (m_method == null) {
                    List<Type> args = Lambda.extract(types, Lambda.on(ITypeResolver.class).get());
                    m_method = m_methods.match(args.toArray(new Type[args.size()]));
                }
                return m_method;
            }
        };
    }
}
