package golem.typesystem;

import java.util.List;

public interface IFunctionTypeResolver extends ITypeResolver {

    public IMethodResolver match(List<ITypeResolver> types);

    public String getName();
}
