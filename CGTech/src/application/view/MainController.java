package application.view;

import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.NodeOrientation;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;


public class MainController implements Initializable{

	@FXML
	private Button button;
	@FXML
	private Button buttonFile;
	@FXML
	private TableView<DataTable> table;

	private TableColumn<DataTable, String> id;
	private TableColumn<DataTable, Double> fluteLength;
	private TableColumn<DataTable, Double> volume;

	@FXML
	private TextField nomFichier;

	@FXML
	private Canvas canva;
	private GraphicsContext gc;

	private ArrayList<ArrayList<Double>> listesX = new ArrayList<ArrayList<Double>>();
	private ArrayList<ArrayList<Double>> listesZ = new ArrayList<ArrayList<Double>>();
	private ArrayList<ArrayList<String>> toolList = new ArrayList<ArrayList<String>>();		


	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {}

	public void init() {
		//TableView, pour afficher les données
		this.id = new TableColumn<DataTable, String>("ID");
		this.fluteLength = new TableColumn<DataTable, Double>("Flute Length");
		this.volume = new TableColumn<DataTable, Double>("Volume");
		this.id.setCellValueFactory(new PropertyValueFactory<DataTable, String>("id"));
		this.fluteLength.setCellValueFactory(new PropertyValueFactory<DataTable, Double>("fluteLength"));
		this.volume.setCellValueFactory(new PropertyValueFactory<DataTable, Double>("volume"));
		table.getColumns().add(this.id);
		table.getColumns().add(this.fluteLength);
		table.getColumns().add(this.volume);
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		
		//Canvas, pour le tracé des figures
		this.canva.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
	    this.canva.setRotate(180);
		this.gc = this.canva.getGraphicsContext2D();
		this.gc.setLineWidth(0.5);
		
	}

	@FXML
	public void onClick() throws ParserConfigurationException, SAXException, IOException {
		//Chargement du fichier XML, calculs et affichage
		try {
			lecture(nomFichier.getText());
			ArrayList<Double> listeVolumes = calculVolume(toolList);
			affichage(toolList, listeVolumes);
		}catch(Exception e) {
			Alert erreurDialog = new Alert(AlertType.ERROR);
			erreurDialog.setContentText("Le fichier n'a pas pu être chargé.\nSeul format accepté : XML");
			erreurDialog.showAndWait();
		}
	}

	@FXML
	public void onChoisir() {
		FileChooser fileDialog = new FileChooser();
		File file = fileDialog.showOpenDialog(null);
		if(file!=null) {
			this.nomFichier.setText(file.getAbsolutePath());
		}
	}

	@FXML
	public void onDessiner() throws ParserConfigurationException, SAXException, IOException {
		//Récupération de la ligne selectionné et de l'index de l'outil correspondant
		DataTable ligne = table.getSelectionModel().getSelectedItem();  
		if(ligne!=null) {
			int index = toolList.get(0).indexOf(ligne.getId());
			//Dessin
			gc.clearRect(0, 0, canva.getWidth(), canva.getHeight());
			for(int j=0; j<this.listesX.get(index).size()-1;j++) {
				if(j<this.listesX.get(index).size()-2) {
					gc.strokeLine(
							this.listesX.get(index).get(j),
							this.listesZ.get(index).get(j),
							this.listesX.get(index).get(j+1),
							this.listesZ.get(index).get(j+1)
					);
				}else {
					gc.strokeLine(
							this.listesX.get(index).get(j),
							this.listesZ.get(index).get(j),
							this.listesX.get(index).get(0),
							this.listesZ.get(index).get(0)
					);
				}
			}
		}
	}

	private void affichage(ArrayList<ArrayList<String>> toolList, ArrayList<Double> listeVolumes) {
		for(int i = 0; i<toolList.get(0).size(); i++) {
			table.getItems().add(new DataTable(toolList.get(0).get(i), Double.parseDouble(toolList.get(1).get(i)), listeVolumes.get(i)));
		}

	}

	private ArrayList<Double> calculVolume(ArrayList<ArrayList<String>> toolList) {
		//La liste contient : 1. ID  2. FluteLength (Y)
		//On a de plus accès aux attributs représentant les listes de coordonnées par tool (X et Z)
		//On calcul le volume en sachant déjà quel type d'aire on va calculer (on connait donc
		//déjà les outils que l'on manipulent).
		//On remarque aussi que chaque figure démarre du coin supérieur droit, points que l'on considérera
		//à chaque fois comme p0.
		ArrayList<Double> listeVolume = new ArrayList<Double>();

		//On commence par T1_66. On découpe cette figure en un grand rectangle, auquel on soustraira un triangle
		//Calcul du rectangle
		double L1 = calculerLongueur(listesX.get(0).get(0), listesX.get(0).get(1), listesZ.get(0).get(0), listesZ.get(0).get(1));
		double l1 = calculerLongueur(listesX.get(0).get(1), listesX.get(0).get(2), listesZ.get(0).get(1), listesZ.get(0).get(2));
		double aireIntermediaire1 = L1*l1;
		//Calcul du triangle, formé par les points p2 et p3, se rejoignant en formant un angle droit, sur un point nommé o.
		//(p4,O) forme une droite perpendiculaire à (p2,O). On a donc O(x(p2), z(p3)).
		double xO = listesX.get(0).get(2);
		double zO = listesZ.get(0).get(3);
		double l2 = calculerLongueur(listesX.get(0).get(2), xO, listesZ.get(0).get(2), zO);
		double L2 = calculerLongueur(listesX.get(0).get(3), xO, listesZ.get(0).get(3), zO);
		double aireIntermediaire2 = (l2*L2)/2;
		listeVolume.add((aireIntermediaire1-aireIntermediaire2)*Double.parseDouble(toolList.get(1).get(0)));


		//Pour T1_67, on va découper le figure en deux rectangles, puis additionner les aires.
		//Premier rectangle, composé de p0, p1, p5 et un autre point, qu'on nommera M.
		//(p5,M) forme une droite perpendiculaire à (p1,M). On a donc M(x(p1), z(p5)
		double xM = listesX.get(1).get(1);
		double zM = listesZ.get(1).get(4);
		double l3 = calculerLongueur(xM, listesX.get(1).get(1), zM, listesZ.get(1).get(1));
		double L3 = calculerLongueur(xM, listesX.get(1).get(4), zM, listesZ.get(1).get(4));
		double aireIntermediaire3 = l3*L3;
		//Toujours par rapport au point M, on va calculer le rectangle composé des points
		//p2,p3,p4,M
		double l4 = calculerLongueur(xM, listesX.get(1).get(2), zM, listesZ.get(1).get(2));
		double L4 = calculerLongueur(xM, listesX.get(1).get(4), zM, listesZ.get(1).get(4));
		double aireIntermediaire4 = l4*L4;
		listeVolume.add((aireIntermediaire3+aireIntermediaire4)*Double.parseDouble(toolList.get(1).get(1)));


		//Pour T1_68, on va utiliser exactement la même technique que pour T1_66
		//Calcul du rectangle
		double L5 = calculerLongueur(listesX.get(2).get(0), listesX.get(2).get(1), listesZ.get(2).get(0), listesZ.get(2).get(1));
		double l5 = calculerLongueur(listesX.get(2).get(1), listesX.get(2).get(2), listesZ.get(2).get(1), listesZ.get(2).get(2));
		double aireIntermediaire5 = L5*l5;
		//Calcul du triangle, formé par les points p2 et p4, se rejoignant en formant un angle droit, sur un point nommé N.
		//(p3,N) forme une droite perpendiculaire à (p2,N). On a donc N(x(p2), z(p3)).
		double xN = listesX.get(2).get(2);
		double zN = listesZ.get(2).get(3);
		double l6 = calculerLongueur(listesX.get(2).get(2), xN, listesZ.get(2).get(2), zN);
		double L6 = calculerLongueur(listesX.get(2).get(3), xN, listesZ.get(2).get(3), zN);
		double aireIntermediaire6 = (l6*L6)/2;
		listeVolume.add((aireIntermediaire5-aireIntermediaire6)*Double.parseDouble(toolList.get(1).get(2)));

		return listeVolume;
	}

	private double calculerLongueur(double x, double x2, double y, double y2) {
		return Math.sqrt(Math.pow((x2-x), 2) + Math.pow((y2-y), 2));
	}

	private void lecture(String nomFichier) throws ParserConfigurationException, SAXException, IOException {
		this.toolList.clear();
		toolList.add(new ArrayList<String>());
		toolList.add(new ArrayList<String>());
		File fichier =  new File(nomFichier);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(fichier);
		doc.getDocumentElement().normalize();

		NodeList nodeList = doc.getElementsByTagName("Tool");
		for(int i = 0; i<nodeList.getLength(); i++) {
			Node tool = nodeList.item(i);
			Element toolElement = (Element)tool;
			if(!toolElement.getAttribute("ID").equals("")){
				toolList.get(0).add(toolElement.getAttribute("ID"));
				toolList.get(1).add(toolElement.getElementsByTagName("FluteLength").item(0).getTextContent());
				NodeList listePt = toolElement.getElementsByTagName("Pt");
				this.listesX.add(new ArrayList<Double>());
				this.listesZ.add(new ArrayList<Double>());
				for(int j = 0; j < listePt.getLength(); j++) {
					Node pt = listePt.item(j);
					Element ptElement = (Element)pt;
					this.listesX.get(i).add(Double.parseDouble(ptElement.getElementsByTagName("X").item(0).getTextContent()));
					this.listesZ.get(i).add(Double.parseDouble(ptElement.getElementsByTagName("Z").item(0).getTextContent()));
				}
			}
		}
	}
}
