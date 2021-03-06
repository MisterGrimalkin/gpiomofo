package net.amarantha.gpiomofo.factory;

import com.esotericsoftware.yamlbeans.YamlReader;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import net.amarantha.gpiomofo.annotation.Named;
import net.amarantha.gpiomofo.annotation.Parameter;
import net.amarantha.gpiomofo.factory.entity.ScenarioBuilderException;
import net.amarantha.gpiomofo.scenario.Scenario;
import net.amarantha.gpiomofo.target.Target;
import net.amarantha.gpiomofo.trigger.CompositeTrigger;
import net.amarantha.gpiomofo.trigger.Trigger;
import net.amarantha.utils.properties.PropertiesService;
import net.amarantha.utils.properties.entity.PropertyNotFoundException;
import net.amarantha.utils.reflection.ReflectionUtils;
import net.amarantha.utils.service.ServiceFactory;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static net.amarantha.gpiomofo.core.Constants.SCENARIO;
import static net.amarantha.utils.properties.PropertiesService.getArgumentValue;
import static net.amarantha.utils.reflection.ReflectionUtils.iterateAnnotatedFields;
import static net.amarantha.utils.reflection.ReflectionUtils.reflectiveSet;
import static net.amarantha.utils.shell.Utility.log;

@Singleton
public class ScenarioBuilder {

    @Inject private Injector injector;
    @Inject private PropertiesService props;
    @Inject private ServiceFactory services;
    @Inject private TriggerFactory triggers;
    @Inject private TargetFactory targets;
    @Inject private LinkFactory links;

    //////////
    // Load //
    //////////

    public Scenario loadScenario() {
        return loadScenario(getScenarioName());
    }

    public Scenario loadScenario(String name) {
        Scenario scenario = buildScenario(name);
        services.injectServices(scenario);
        props.injectPropertiesOrExit(scenario);

        buildComponentsFromConfig(scenario);
        injectComponents(scenario);
        scenario.setup();
        scenario.logSetup();
        return scenario;
    }

    private String getScenarioName() {
        String scenarioName = "";
        try {
            String commandLineClassName = getArgumentValue(SCENARIO);
            if (commandLineClassName == null) {
                scenarioName = props.getString("Scenario");
            } else {
                scenarioName = commandLineClassName;
            }
            props.getStringOrDefault("Scenario", scenarioName);
        } catch (PropertyNotFoundException e) {
            System.out.println("No Scenario specified\n\nSee: gpiomofo.sh -help\n");
            System.exit(1);
        }
        return scenarioName;
    }

    @SuppressWarnings("unchecked")
    private Scenario buildScenario(String className) {
        Class<Scenario> clazz = ReflectionUtils.getClass(props.getStringOrDefault("ScenarioPackage", DEFAULT_SCENARIO_PACKAGE) + "." + className);
        if ( clazz==null ) {
            clazz = ReflectionUtils.getClass(DEFAULT_SCENARIO_PACKAGE + "." + className);
            if ( clazz==null ) {
                clazz = Scenario.class;
            }
        }
        Scenario scenario = injector.getInstance(clazz);
        scenario.setName(className);
        return scenario;
    }

    ///////////////
    // Injection //
    ///////////////

    private void injectComponents(Scenario scenario) {
        iterateAnnotatedFields(scenario, Named.class,
            (field, annotation)->
                reflectiveSet(scenario, field, annotation.value(),
                    (type, name)->{
                        if ( Trigger.class.isAssignableFrom(type) ) {
                            return triggers.get(name);
                        } else if ( Target.class.isAssignableFrom(type) ) {
                            return targets.get(name);
                        }
                        return null;
                    }
                )
        );
    }

    ////////////////////////
    // YAML Configuration //
    ////////////////////////

    @SuppressWarnings("unchecked")
    private void buildComponentsFromConfig(Scenario scenario) {
        Map<String, Map> config = null;
        String filename = props.getStringOrDefault("ScenariosDirectory", "scenarios")+"/"+scenario.getName()+".yaml";
        try (FileReader reader =new FileReader(filename)) {
            YamlReader yaml = new YamlReader(reader);
            config = (Map<String, Map>) yaml.read();
            scenario.setConfigFilename(filename);
        } catch (FileNotFoundException e) {
            if ( scenario.getClass()==Scenario.class ) {
                log("No configuration found for custom scenario '"+scenario.getName()+"'");
                System.exit(1);
            }
        } catch (IOException e) {
            log("Could not read configuration.\n" + e.getMessage());
            System.exit(1);
        }
        processConfig(scenario, config);
    }

    private void processConfig(Scenario scenario, Map<String, Map> config) {
        injectParameters(scenario, config);
        processTriggers(config);
        processCompositeTriggers(config);
        processTargets(config);
        processLinks(config);
    }

        @SuppressWarnings("unchecked")
    private void injectParameters(Scenario scenario, Map<String, Map> config) {
        if ( config!=null && config.get("Parameters")!=null ) {
            scenario.setParameters((HashMap<String, String>) config.get("Parameters"));
            iterateAnnotatedFields(scenario, Parameter.class,
                    (field,annotation)-> reflectiveSet(scenario, field, scenario.getParameters().get(annotation.value()))
            );
        }
    }

    //////////////
    // Triggers //
    //////////////

    interface TreeConsumer<T> {
        void accept(String name, Map<String, T> config) throws ScenarioBuilderException;
    }

    @SuppressWarnings("unchecked")
    private <T> void iterateTree(String key, Map<String, Map> config, TreeConsumer<T> consumer) {
        List<ScenarioBuilderException> errors = new LinkedList<>();
        if ( config!=null ) {
            HashMap<String, T> subConfig = (HashMap<String, T>) config.get(key);
            if ( subConfig!=null ) {
                for (Entry<String, T> entry : subConfig.entrySet()) {
                    try {
                        consumer.accept(entry.getKey(), (HashMap<String, T>) entry.getValue());
                    } catch (ScenarioBuilderException e) {
                        errors.add(e);
                    }
                }
            }
        }
        checkErrors(errors);
    }

    private void processTriggers(Map<String, Map> config) {
        iterateTree("Triggers", config, this::buildTrigger);
    }

    private void buildTrigger(String name, Map<String, String> config) throws ScenarioBuilderException {
        String type = config.get("type");
        Class<Trigger> triggerClass = ReflectionUtils.getClass(props.getStringOrDefault("TriggerPackage", DEFAULT_TRIGGER_PACKAGE)+"."+type+"Trigger");
        if ( triggerClass==null ) {
            triggerClass = ReflectionUtils.getClass(DEFAULT_TRIGGER_PACKAGE+"."+type+"Trigger");
            if ( triggerClass==null ) {
                throw new ScenarioBuilderException("Unknown type '" + type + "' for trigger '" + name + "'");
            }
        }
        triggers.create(name, triggerClass, config);
    }

    ////////////////////////
    // Composite Triggers //
    ////////////////////////

    @SuppressWarnings("unchecked")
    private void processCompositeTriggers(Map<String, Map> config) {
//        iterateTree("CompositeTriggers", config, (name, compTrigConfig) -> {
//                List<String> triggerNames = ((HashMap<String, List>)compTrigConfig).get("triggers");
//                buildCompositeTrigger(name, triggerNames, (HashMap<String, String>) entry.getValue());
//        });
        List<ScenarioBuilderException> errors = new LinkedList<>();
        if ( config!=null ) {
            HashMap<String, Map> compTriggerConfigs = (HashMap<String, Map>) config.get("CompositeTriggers");
            if ( compTriggerConfigs!=null ) {
                for (Entry<String, Map> entry : compTriggerConfigs.entrySet()) {
                    try {
                        List<String> triggerNames = ((HashMap<String, List>)entry.getValue()).get("triggers");
                        buildCompositeTrigger(entry.getKey(), triggerNames, (HashMap<String, String>) entry.getValue());
                    } catch (ScenarioBuilderException e) {
                        errors.add(e);
                    }
                }
            }
        }
        checkErrors(errors);
    }

    private void buildCompositeTrigger(String name, List<String> triggerNames, Map<String, String> config) throws ScenarioBuilderException {
        CompositeTrigger trigger = triggers.create(name, CompositeTrigger.class, config);
        triggerNames.forEach((tn) -> trigger.addTriggers(triggers.get(tn)));
    }

    /////////////
    // Targets //
    /////////////

    @SuppressWarnings("unchecked")
    private void processTargets(Map<String, Map> config) {
        iterateTree("Targets", config, this::buildTarget);
    }

    private void buildTarget(String name, Map<String, String> config) throws ScenarioBuilderException {
        String type = config.get("type");
        Class<Target> targetClass = ReflectionUtils.getClass(props.getStringOrDefault("TargetPackage", DEFAULT_TARGET_PACKAGE)+"."+type+"Target");
        if ( targetClass==null ) {
            targetClass = ReflectionUtils.getClass(DEFAULT_TARGET_PACKAGE+"."+type+"Target");
            if ( targetClass==null ) {
                throw new ScenarioBuilderException("Unknown type '" + type + "' for target '" + name + "'");
            }
        }
        targets.create(name, targetClass, config);
    }

    ///////////
    // Links //
    ///////////

    @SuppressWarnings("unchecked")
    private void processLinks(Map<String, Map> config) {
        if ( config!=null ) {
            HashMap<String, List<String>> linkConfigs = (HashMap<String, List<String>>) config.get("Links");
            if ( linkConfigs!=null ) {
                for (Entry<String, List<String>> entry : linkConfigs.entrySet() ) {
                    buildLink(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    private void buildLink(String triggerName, List<String> targetNames) {
        links.link(triggerName, targetNames.toArray(new String[targetNames.size()]));
    }

    /////////////
    // Utility //
    /////////////

    private void checkErrors(List<ScenarioBuilderException> errors ) {
        if ( !errors.isEmpty() ) {
            log("ERRORS!");
            for ( ScenarioBuilderException e : errors ) {
                log("    "+e.getMessage());
            }
            System.exit(1);
        }
    }

    private static final String DEFAULT_SCENARIO_PACKAGE = "net.amarantha.gpiomofo.scenario";
    private static final String DEFAULT_TRIGGER_PACKAGE = "net.amarantha.gpiomofo.trigger";
    private static final String DEFAULT_TARGET_PACKAGE = "net.amarantha.gpiomofo.target";

}