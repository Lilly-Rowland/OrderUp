package com.example.game;

import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

public class MenuManager {

	public Button createMenuButton() {
		Button menuButton = new Button("Menu");

		ContextMenu contextMenu = new ContextMenu();
		contextMenu.getItems().addAll(
			new MenuItem("Herbel Tea"),
			new MenuItem("Lavender Lemonade"),
			new MenuItem("Spring Roll"),
			new MenuItem("Stuffed Zucchini Blossoms"),
			new MenuItem("Sweet Pea Soup"),
			new MenuItem("The Impossible Garden Burger"),
			new MenuItem("Wild Foraged Truffle & Mushroom Pappardelle"),
			new MenuItem("Harvest Flatbread"),
			new MenuItem("Seasonal Fruit Tart"),
			new MenuItem("Chocolate Avocado Mousse")
		);

		menuButton.setOnAction(e -> contextMenu.show(menuButton, Side.BOTTOM, 0, 0));
		return menuButton;
	}
}
