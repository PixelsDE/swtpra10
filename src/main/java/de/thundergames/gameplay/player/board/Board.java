/*
 * Copyright Notice for SwtPra10
 * Copyright (c) at ThunderGames | SwtPra10 2022
 * File created on 13.01.22, 15:20 by Carina Latest changes made by Carina on 13.01.22, 15:20 All contents of "Board" are protected by copyright. The copyright law, unless expressly indicated otherwise, is
 * at ThunderGames | SwtPra10. All rights reserved
 * Any type of duplication, distribution, rental, sale, award,
 * Public accessibility or other use
 * requires the express written consent of ThunderGames | SwtPra10.
 */

package de.thundergames.gameplay.player.board;

import de.thundergames.playmechanics.map.Field;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Board extends Group {
  private final int radius;
  // TODO:: make a partial type for nodes that decouples logic from UI
  private final HashSet<Node> nodes;
  private final ArrayList<Edge> edges;
  //private final Map map;
  private final HashMap<List<Integer>, NodeType> nodesType;
  private final ArrayList<PlayerModel> players;
  private double width;
  private double height;

  /**
   * @param radius
   * @param width
   * @param height
   * @author Issam, Alp, Dila
   * @use generate nodes and edges
   */
  public Board(final int radius, final double width, final double height, @NotNull final HashMap<List<Integer>, NodeType> nodesType, @NotNull final ArrayList<PlayerModel> players) {
    super();
    this.radius = radius;
    this.width = width;
    this.height = height;
    this.nodes = new HashSet<>();
    this.edges = new ArrayList<>();
    this.players = players;
    this.nodesType = nodesType;
  }

  public void setContainerBackground(@NotNull final Pane container, @NotNull final String bgSpritePath) {
    var backgroundImage = new BackgroundImage(new Image(Utils.getSprite(bgSpritePath), 100, 100, false, true),
      BackgroundRepeat.REPEAT,
      BackgroundRepeat.REPEAT,
      BackgroundPosition.CENTER,
      BackgroundSize.DEFAULT);
    container.setBackground(new Background(backgroundImage));
  }

  /**
   * @param node
   * @author Issam, Alp, Dila
   */
  public List<Node> getNodeNeighbors(@NotNull final Node node) {
    var nodeID = node.getNodeID();
    var nodeRow = node.getRow();
    var rowOffset = nodeRow < this.radius + 1 ? this.radius + nodeRow : 3 * this.radius + 2 - nodeRow;
    var maxPossibleID = 3 * (int) Math.pow(this.radius, 2) + 3 * this.radius + 1;
    // Get list of possible neighbors
    var possibleNeighborsIDs = new ArrayList<>(List.of(nodeID - 1, nodeID + 1, nodeID + rowOffset));
    if (nodeRow < this.radius + 1) {
      possibleNeighborsIDs.add(nodeID + rowOffset + 1);
    } else {
      possibleNeighborsIDs.add(nodeID + rowOffset - 1);
    }
    var possibleNeighbors = this.nodes.stream().filter(n -> possibleNeighborsIDs.contains(n.getNodeID())).collect(Collectors.toList());
    // Filter out invalid neighbors
    var isValidID = (Function<Node, Boolean>) neighbor -> neighbor.getNodeID() > 0 && neighbor.getNodeID() <= maxPossibleID && neighbor.getNodeID() > nodeID;
    var isNextEdge = (Function<Node, Boolean>) neighbor -> (neighbor.getNodeID() == nodeID + 1 && neighbor.getRow() > nodeRow) || neighbor.getRow() - nodeRow > 1;
    var isAdjacentSameRow = (Function<Node, Boolean>) neighbor -> (neighbor.getNodeID() > nodeID + 1 && neighbor.getRow() == nodeRow);
    return possibleNeighbors.stream().filter(neighbor -> isValidID.apply(neighbor) && !isNextEdge.apply(neighbor) && !isAdjacentSameRow.apply(neighbor)).distinct().collect(Collectors.toList());
  }

  /**
   * @param numberOfNodes
   * @param maxNumberOfNodes
   * @param row
   * @return returns the 2d Points
   * @author Issam, Alp, Dila
   */
  private Point2D[] getNodesPosition(final int numberOfNodes, final int maxNumberOfNodes, final int row) {
    // Determine margin between nodes
    var displayHeight = this.height;
    var maxAreaCoveredByNodes = maxNumberOfNodes * 15; //TODO: change constant to actual node radius
    var verticalMargin = (displayHeight - maxAreaCoveredByNodes - 100) / maxNumberOfNodes;
    var horizontalMargin = verticalMargin / 2;
    var edgeMargins = maxNumberOfNodes - numberOfNodes;
    var points = new Point2D[numberOfNodes];
    var widthSoFar = edgeMargins * horizontalMargin;
    for (var i = 0; i < numberOfNodes; i++) {
      points[i] = new Point2D(widthSoFar, row * verticalMargin + 50);
      widthSoFar += 2 * horizontalMargin;
    }
    return points;
  }

  /**
   * @author Issam, Alp, Dila
   * @use generates the nodes
   */
  public void generateNodes() {
    var numberOfGridRows = this.radius * 2 + 1;
    var startID = 1;
    for (var i = 0; i < numberOfGridRows; i++) {
      var numberOfGridCols = i <= this.radius ? this.radius + i + 1 : this.radius + numberOfGridRows - i;
      var nodesPositions = getNodesPosition(numberOfGridCols, numberOfGridRows, i);
      var shift = i > this.radius ? numberOfGridRows - numberOfGridCols : 0;
      for (var j = 0; j < numberOfGridCols; j++) {
        var nodeType = this.nodesType.get(List.of(i, j)) != null
          ? this.nodesType.get(List.of(i, j))
          : NodeType.DEFAULT;
        this.nodes.add(new Node(startID + j, nodesPositions[j].getX(), nodesPositions[j].getY(), nodeType, i + 1, new Field(i, j + shift)));
      }
      startID += numberOfGridCols;
    }
  }

  /**
   * @author Issam, Alp, Dila
   * @use generates the Edges
   */
  public void generateEdges() {
    for (var node : nodes) {
      var neighbors = getNodeNeighbors(node);
      for (var neighbor : neighbors) {
        this.edges.add(new Edge(node.getCenterX(), node.getCenterY(), neighbor.getCenterX(), neighbor.getCenterY()));
      }
    }
  }

  /**
   * @author Issam, Alp, Dila
   * @use generates the moles
   */
  public void generateMoles() {
    // Moles need to be set on each state mutation and should have the same id as the corresponding node
    for (var p : this.players) {
      for (var mole : p.getMoles()) {
        var correspondingNode = getNodeByField(mole.getMole().getPosition());
        assert correspondingNode != null;
        mole.setLayoutX(correspondingNode.getCenterX() - mole.getSize() / 2);
        mole.setLayoutY(correspondingNode.getCenterY() - mole.getSize() / 2);
        mole.render();
      }
    }
  }

  public void onResize(final double width, final double height) {
    this.width = width;
    this.height = height;
    // TODO: Debounce rendering to avoid multiple successive renders when resizing
    this.render();
  }

  private void clearBoard() {
    this.nodes.clear();
    this.edges.clear();
    this.getChildren().clear();
  }

  /**
   * @author Issam, Alp, Dila
   * @use renders the board
   */
  public void render() {
    // On each render clear nodes and edges (this is not ideal performance-wise ! )
    this.clearBoard();
    // generate edges and nodes
    this.generateNodes();
    this.generateEdges();
    // display edges and nodes
    this.edges.forEach(edge -> this.getChildren().add(edge));
    this.nodes.forEach(node -> this.getChildren().add(node));
    // display moles
    this.generateMoles();
    this.players.forEach(player -> this.getChildren().addAll(player.getMoles()));
    // display markers
    this.players.forEach(PlayerModel::updateMarker);
    this.players.forEach(player -> this.getChildren().addAll((player.getMarkers())));
  }

  private Node getNodeByField(final Field field) {
    for (var node : nodes) {
      if (node.getField().getX() == field.getX() && node.getField().getY() == field.getY()) {
        return node;
      }
    }
    return null;
  }

  @SuppressWarnings("OptionalGetWithoutIsPresent")
  public PlayerModel getCurrentPlayerModel(int currentPlayerID) {
    return this.players.stream().filter(playerModel -> playerModel.getPlayer().getClientID() == currentPlayerID).findFirst().get();
  }

  @SuppressWarnings("OptionalGetWithoutIsPresent")
  public void moveMole(Field from, Field to, int currentPlayerID) {
    var currentPlayerModel = getCurrentPlayerModel(currentPlayerID);
    var moleToBeMoved = currentPlayerModel.getMoles().stream().filter(_mole -> _mole.hasSameField(from)).findFirst().get();
    var nodeTo = getNodeByField(to);
    currentPlayerModel.getMoles().remove(moleToBeMoved);
    this.getChildren().remove(moleToBeMoved);
    // Update mole
    moleToBeMoved.getMole().setPosition(to);
    assert nodeTo != null;
    moleToBeMoved.setLayoutX(nodeTo.getCenterX() - moleToBeMoved.getSize() / 2);
    moleToBeMoved.setLayoutY(nodeTo.getCenterY() - moleToBeMoved.getSize() / 2);
    currentPlayerModel.getMoles().add(moleToBeMoved);
    this.getChildren().add(moleToBeMoved);
  }

  public void placeMole(MoleModel mole) {
    var currentPlayerModel = getCurrentPlayerModel(mole.getMole().getPlayer().getClientID());
    currentPlayerModel.getMoles().add(mole);
    var nodeTo = getNodeByField(mole.getMole().getPosition());
    assert nodeTo != null;
    mole.setLayoutX(nodeTo.getCenterX() - mole.getSize() / 2);
    mole.setLayoutY(nodeTo.getCenterY() - mole.getSize() / 2);
    mole.render();
    this.getChildren().add(mole);
  }
}
