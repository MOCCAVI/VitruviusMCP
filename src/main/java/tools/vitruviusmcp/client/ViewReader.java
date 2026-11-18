package tools.vitruviusmcp.client;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

import tools.vitruv.framework.remote.client.VitruvClient;
import tools.vitruv.framework.remote.client.VitruvClientFactory;
import tools.vitruv.framework.views.View;
import tools.vitruv.framework.views.ViewSelector;
import tools.vitruv.framework.views.ViewType;
import tools.vitruv.methodologisttemplate.model.model.ModelPackage;
import tools.vitruv.methodologisttemplate.model.model2.Model2Package;

/**
 * Task 2: reads from a running Vitruv-Server.
 * Lists the view types, opens each view with all elements selected,
 * and prints its contents as a tree (type + name), using EMF reflection only.
 */
public class ViewReader {

  private static final String HOST = "localhost";
  private static final int PORT = 8080;

  public static void main(String[] args) throws Exception {
    registerMetamodels();

    Path tempDir = Files.createTempDirectory("vitruv-client");
    VitruvClient client = VitruvClientFactory.create(HOST, PORT, tempDir);

    // 1. Which view types does the server offer? (first real request to the server)
    Collection<ViewType<?>> viewTypes = client.getViewTypes();
    System.out.println("View types: " + viewTypes.stream().map(ViewType::getName).toList());

    // 2. Open and print every view type, one after another
    for (ViewType<?> viewType : viewTypes) {
      System.out.println();
      System.out.println("--- View '" + viewType.getName() + "' ---");
      try {
        printView(client, viewType);
      } catch (Exception e) {
        // Keep going with the other view types, but show what went wrong
        System.out.println("Could not read view: " + e);
        e.printStackTrace();
      }
    }
  }

  /**
   * Outside Eclipse, generated EMF packages are not registered automatically.
   * Touching eINSTANCE registers each metamodel under its nsURI, so the client
   * can deserialize the models it receives from the server.
   */
  private static void registerMetamodels() {
    System.out.println("Registered: " + ModelPackage.eINSTANCE.getNsURI());
    System.out.println("Registered: " + Model2Package.eINSTANCE.getNsURI());
  }

  private static void printView(VitruvClient client, ViewType<?> viewType) throws Exception {
    ViewSelector selector = client.createSelector(viewType);

    // Select everything the selector offers (model roots of the V-SUM)
    Collection<EObject> selectable = selector.getSelectableElements();
    System.out.println("Selectable elements: " + selectable.size());
    for (EObject element : selectable) {
      selector.setSelected(element, true);
    }

    // try-with-resources closes the view on the server afterwards
    try (View view = selector.createView()) {
      Collection<EObject> roots = view.getRootObjects();
      if (roots.isEmpty()) {
        System.out.println("(empty)");
      }
      for (EObject root : roots) {
        printTree(root, 0);
      }
    }
  }

  /** Prints an element and everything it contains, indented by depth. */
  private static void printTree(EObject object, int depth) {
    System.out.println("  ".repeat(depth) + describe(object));
    for (EObject child : object.eContents()) {
      printTree(child, depth + 1);
    }
  }

  /** "Component \"router1\"" - type name plus the name attribute if the class has one. */
  private static String describe(EObject object) {
    String type = object.eClass().getName();
    EStructuralFeature nameFeature = object.eClass().getEStructuralFeature("name");
    if (nameFeature == null) {
      return type;
    }
    Object name = object.eGet(nameFeature);
    return type + " \"" + name + "\"";
  }
}
