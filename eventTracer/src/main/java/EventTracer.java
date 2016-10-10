import java.awt.*;
import java.beans.BeanInfo;
import java.beans.EventSetDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by ScorpionOrange on 2016/10/04.
 */
public class EventTracer {
    private InvocationHandler handler;

    public EventTracer(){
        // the handler for all event proxies
        handler = new InvocationHandler(){
            public Object invoke(Object proxy, Method method, Object[] args){
                System.out.println(method + ":" + args[0]);
                return null;
            }
        };
    }

    /**
     * Adds event tracers for all events to which this component and its children can listen
     * @param component a component
     */
    public void add(Component component){
        try{
            // get all events to which this component can listen
            BeanInfo info = Introspector.getBeanInfo(component.getClass());

            EventSetDescriptor[] eventSets = info.getEventSetDescriptors();
            for(EventSetDescriptor eventSetDescriptor : eventSets){
                addListener(component, eventSetDescriptor);
            }
        }
        catch (IntrospectionException e){}

        // ok not to add listeners if exception is thrown

        if(component instanceof Container){
            // get all children and call add recursively
            for(Component comp : ((Container)component).getComponents()){
                add(comp);
            }
        }
    }

    /**
     * Add a listener to the given event set
     * @param component a component
     * @param eventSet a descriptor of a listener interface
     */
    public void addListener(Component component, EventSetDescriptor eventSet){
        // make proxy object for this listener type and route all call to the handler
        Object proxy = Proxy.newProxyInstance(null, new Class[]{eventSet.getListenerType()}, handler);

        // add the proxy as a listener to the component
        Method addListenerMethod = eventSet.getAddListenerMethod();
        try{
            addListenerMethod.invoke(component, proxy);
        }
        catch (ReflectiveOperationException e){}
        // ok not to add listener if exception is thrown
    }
}
