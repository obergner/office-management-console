package io.obergner.office.accounts.subaccounts.simsme;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.io.Serializable;

import static org.springframework.util.Assert.notNull;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "action")
@JsonTypeIdResolver(SimsmeAccountRefModification.SimsmeAccountRefCreationTypeIdResolver.class)
public abstract class SimsmeAccountRefModification implements Serializable {

    public enum Action {

        none,

        referenceExisting,

        createNew,

        deleteReference
    }

    @JsonIgnore
    public final Action action;

    protected SimsmeAccountRefModification(final Action action) {
        notNull(action, "Argument 'action' must not be null");
        this.action = action;
    }

    static class SimsmeAccountRefCreationTypeIdResolver implements TypeIdResolver {

        private JavaType baseType;

        @Override
        public void init(final JavaType baseType) {
            this.baseType = baseType;
        }

        @Override
        public String idFromValue(final Object value) {
            return idFromValueAndType(value, value.getClass());
        }

        @Override
        public String idFromValueAndType(final Object value, final Class<?> suggestedType) {
            if (!(value instanceof SimsmeAccountRefModification)) {
                throw new IllegalArgumentException("Supplied value [" + value + "] needs to be a [" + SimsmeAccountRefModification.class.getName() + "] but is in fact a [" + value.getClass().getName() + "]");
            }
            return SimsmeAccountRefModification.class.cast(value).action.toString();
        }

        @Override
        public String idFromBaseType() {
            return idFromValueAndType(null, this.baseType.getRawClass());
        }

        @Override
        public JavaType typeFromId(final String id) {
            final Action action = Action.valueOf(id);
            switch (action) {
                case none:
                    return TypeFactory.defaultInstance().constructSpecializedType(this.baseType, NoneSimsmeAccountRefCreation.class);
                case referenceExisting:
                    return TypeFactory.defaultInstance().constructSpecializedType(this.baseType, ExistingSimsmeAccountRefCreation.class);
                case createNew:
                    return TypeFactory.defaultInstance().constructSpecializedType(this.baseType, CreateNewSimsmeAccountRefCreation.class);
                case deleteReference:
                    return TypeFactory.defaultInstance().constructSpecializedType(this.baseType, SimsmeAccountRefDeletion.class);
                default:
                    throw new IllegalArgumentException("Unsupported action: " + action);
            }
        }

        @Override
        public JsonTypeInfo.Id getMechanism() {
            return JsonTypeInfo.Id.CUSTOM;
        }
    }
}
