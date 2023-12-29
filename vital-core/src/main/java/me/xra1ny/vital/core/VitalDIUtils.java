package me.xra1ny.vital.core;

import lombok.NonNull;
import lombok.extern.java.Log;
import me.xra1ny.vital.core.annotation.VitalDI;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Class for dependency injection utilities.
 *
 * @author xRa1ny
 */
@Log
public class VitalDIUtils {
    /**
     * Attempts to create a dependency injected instance of the supplied class type.
     *
     * @param type The class from which the di instance should be created.
     * @param <T>  The type of the supplied class object.
     * @return An {@link Optional} holding either the newly created instance; or empty.
     */
    public static <T> Optional<T> getDependencyInjectedInstance(@NonNull Class<T> type) {
        if (VitalComponent.class.isAssignableFrom(type)) {
            // check if instance is already existent on base vital manager...
            final Class<? extends VitalComponent> vitalComponentClass = (Class<? extends VitalComponent>) type;
            final VitalCore<?> vitalCore = VitalCore.getVitalCoreInstance();

            final Optional<? extends VitalComponent> optionalVitalComponent = vitalCore.getVitalComponent(vitalComponentClass);

            if (optionalVitalComponent.isPresent()) {
                return (Optional<T>) optionalVitalComponent;
            }

            // if not, attempt to get instance from any manager on vital...
            for (VitalComponentListManager<?> vitalComponentListManager : vitalCore.getVitalComponentList(VitalComponentListManager.class)) {
                if (!vitalComponentListManager.managedType().isAssignableFrom(vitalComponentClass)) {
                    continue;
                }

                final Optional<? extends VitalComponent> optionalVitalComponent1 = vitalComponentListManager.getVitalComponent(vitalComponentClass);

                if (optionalVitalComponent1.isPresent()) {
                    return (Optional<T>) optionalVitalComponent1;
                }
            }
        }

        // check if component is viable for automatic dependency injection (DI).
        if (!type.isAnnotationPresent(VitalDI.class)) {
            log.severe(type + " is not annotated with VitalDI");

            return Optional.empty();
        }

        // if the class we are trying to automatically DI is abstract, cancel operation since no instance can be created without throwing exceptions.
        if (Modifier.isAbstract(type.getModifiers())) {
            log.severe(type.getSimpleName() + " is annotated with @VitalDI but abstract and can therefore not be dependency injected by Vital");

            return Optional.empty();
        }

        try {
            // attempt to get default constructor...
            final Constructor<T> defaultConstructor = type.getDeclaredConstructor();

            try {
                final T instance = defaultConstructor.newInstance();

                return Optional.of(instance);
            } catch (Exception e) {
                log.severe("Vital has encountered an internal error while trying to create a dependency injected instance of type " + type);
                log.severe("Is the type instantiable? (class, not abstract, public)");
            }
        } catch (NoSuchMethodException e) {
            for (Constructor<?> constructor : type.getDeclaredConstructors()) {
                final List<Object> injectableList = new ArrayList<>();

                for (Parameter parameter : constructor.getParameters()) {
                    if (!VitalComponent.class.isAssignableFrom(parameter.getType()) && !JavaPlugin.class.isAssignableFrom(parameter.getType())) {
                        // ignore when parameter is not of type `JavaPlugin` or `VitalComponent`.
                        break;
                    }

                    if(VitalCore.class.isAssignableFrom(parameter.getType())) {
                        injectableList.add(VitalCore.getVitalCoreInstance());

                        continue;
                    }

                    if (JavaPlugin.class.isAssignableFrom(parameter.getType())) {
                        injectableList.add(VitalCore.getVitalCoreInstance().getJavaPlugin());

                        continue;
                    }

                    if (VitalComponentListManager.class.isAssignableFrom(parameter.getType())) {
                        final Class<? extends VitalComponentListManager<?>> managerClass = (Class<? extends VitalComponentListManager<?>>) parameter.getType();
                        final VitalCore<?> vitalCore = VitalCore.getVitalCoreInstance();

                        final Optional<? extends VitalComponentListManager<?>> optionalVitalComponentListManager = vitalCore.getVitalComponent(managerClass);

                        if (optionalVitalComponentListManager.isPresent()) {
                            injectableList.add(optionalVitalComponentListManager.get());

                            continue;
                        }

                        // if not present, attempt to create a dependency injected instance of it and register it on the base manager.

                        final Optional<? extends VitalComponentListManager<?>> optionalNewVitalComponentListManager = getDependencyInjectedInstance(managerClass);

                        if(optionalNewVitalComponentListManager.isPresent()) {
                            final VitalComponentListManager<?> newVitalComponentListManager = optionalNewVitalComponentListManager.get();

                            vitalCore.registerVitalComponent(newVitalComponentListManager);
                            injectableList.add(newVitalComponentListManager);

                            continue;
                        }
                    }

                    // normal vital component. VitalConfig
                    final Class<? extends VitalComponent> vitalComponentClass = (Class<? extends VitalComponent>) parameter.getType();
                    final VitalCore<?> vitalCore = VitalCore.getVitalCoreInstance();

                    final Optional<? extends VitalComponent> optionalVitalComponent = vitalCore.getVitalComponent(vitalComponentClass);

                    if (optionalVitalComponent.isPresent()) {
                        injectableList.add(optionalVitalComponent.get());

                        continue;
                    }

                    // if not present on base.
                    for (VitalComponentListManager<VitalComponent> vitalComponentListManager : vitalCore.getVitalComponentList(VitalComponentListManager.class)) {
                        if (!vitalComponentListManager.managedType().isAssignableFrom(vitalComponentClass)) {
                            continue;
                        }

                        // enable that manager for potentially missing components.
                        vitalComponentListManager.enable();

                        // afterward attempt to get the instance we're looking for.
                        final Optional<? extends VitalComponent> optionalVitalComponent1 = vitalComponentListManager.getVitalComponent(vitalComponentClass);

                        if(optionalVitalComponent1.isPresent()) {
                            injectableList.add(optionalVitalComponent1.get());

                            continue;
                        }

                        // else attempt to create new instance and register it on manager.
                        final Optional<? extends VitalComponent> optionalNewVitalComponent = getDependencyInjectedInstance(vitalComponentClass);

                        if(optionalNewVitalComponent.isPresent()) {
                            final VitalComponent newVitalComponent = optionalNewVitalComponent.get();

                            vitalComponentListManager.registerVitalComponent(newVitalComponent);
                            injectableList.add(newVitalComponent);

                            continue;
                        }
                    }
                }

                try {
                    // create dependency injected instance.
                    final T instance = (T) constructor.newInstance(injectableList.toArray());

                    return Optional.of(instance);
                } catch (Exception ex) {
                    log.severe("Vital could not create a dependency injected instance of " + type);
                    log.severe("Vital has deciphered the following constructor viable for dependency injection: " + constructor);
                    log.severe("Vital has deciphered the following arguments to inject into said constructor: " + injectableList);
                    log.severe("Did you declare a circular dependency?");
                    log.severe("Are you sure all constructor arguments are viable for dependeny injection? (@VitalDI, @VitalManagerAutoregistered, @VitalAutoregistered, VitalComponent)");
                }
            }
        }

        return Optional.empty();
    }
}

