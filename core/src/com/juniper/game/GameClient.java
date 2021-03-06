package com.juniper.game;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.juniper.game.components.client.AnimatedSprite;
import com.juniper.game.components.client.Movement;
import com.juniper.game.components.client.PlayerControlled;
import com.juniper.game.components.client.Sprite;
import com.juniper.game.components.shared.Bounds;
import com.juniper.game.components.shared.Position;
import com.juniper.game.components.shared.TileID;
import com.juniper.game.network.Network;
import com.juniper.game.network.Network.*;
import com.juniper.game.systems.*;
import com.juniper.game.util.CustomTiledMapRenderer;

import java.io.IOException;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Iterator;
import java.util.TimerTask;

public class GameClient implements ApplicationListener, InputProcessor {
	public Client client;
	final int writeBufferSize = 8192; //Default 8192
	final int objectBufferSize = 2048; //Default 2048 FIXME 10.1.2015 22:30 2048 breaks at about 60 entities

	//Gui
	private Skin skin;
	private Table menuLayout;

	private Table chatLayout;
	private TextArea chatArea;
	private TextField chatField;

	private Table inGameLayout;
	private TextField gameChatField;
	private TextArea messageWindow;

	HashMap<Long,TextField> chatBubbles;

	//Graphics
	float w;
	float h;
	private OrthographicCamera camera;
	private SpriteBatch batch;
	private ShapeRenderer shapeRenderer;
	private Stage stage;
	CustomTiledMapRenderer tiledMapRenderer;

	//World state
	private List<String> nameList;
	GdxWorldData gdxWorldData;
	long playerNetworkID = -1;
	Entity player;

	//Game state
	boolean up;
	boolean down;
	boolean left;
	boolean right;
	HashMap<Long,Component[]> pendingEntitySync;
	String pendingMapChange;
	boolean pendingReset;


	public long tickStartTime;
	public long mapChangeDuration;
	public long syncDuration;
	public long updateDuration;
	public long guiUpdateDuration;

	@Override
	public void create () {
		System.out.println("Internal storage path: " + Gdx.files.internal("data/uiskin.json"));
		System.out.println("Local storage path: " + Gdx.files.getLocalStoragePath());
		System.out.println("External storage path: " + Gdx.files.getExternalStoragePath());
		shapeRenderer = new ShapeRenderer();
		batch = new SpriteBatch();
		skin = new Skin(Gdx.files.internal("data/uiskin.json"));
		stage = new Stage(new ScreenViewport(), batch);
		chatBubbles = new HashMap<>();
		Gdx.input.setInputProcessor(stage);

		w = Gdx.graphics.getWidth();
		h = Gdx.graphics.getHeight();

		camera = new OrthographicCamera();
		camera.setToOrtho(false,w,h);
		camera.update();

		createGUI();

	}

	private void createGUI(){
		Label nameLabel = new Label("Name:", skin);
		final TextField nameText = new TextField("frog", skin);
		Label addressLabel = new Label("Address:", skin);
		final TextField addressText = new TextField("localhost", skin);
		final TextButton chatButton = new TextButton("Chat", skin, "default");
		final TextButton playButton = new TextButton("Play", skin, "default");

		nameText.selectAll();

		nameText.addListener(new InputListener(){
			@Override
			public boolean keyDown(InputEvent event, int keycode){
				if(keycode == Input.Keys.ENTER){
					stage.setKeyboardFocus(addressText);
					addressText.selectAll();
				}
				return true;
			}
		});

		addressText.addListener(new InputListener(){
			@Override
			public boolean keyDown(InputEvent event, int keycode){
				if(keycode == Input.Keys.ENTER){
					playButton.setText("Connecting..");
					joinServer(addressText.getText(), nameText.getText());
					System.out.println("Joining " + addressText.getText());
				}
				return true;
			}
		});

		playButton.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y){
				playButton.setText("Connecting..");
				menuLayout.setVisible(false);
				inGameLayout.setVisible(true);
				Gdx.input.setInputProcessor(GameClient.this);
				//Join server and spawn on default map
				joinServer(addressText.getText(), nameText.getText(),"untitled.tmx");
				System.out.println("Joining play: " + addressText.getText());
			}
		});

		chatButton.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y){
				chatButton.setText("Connecting..");
				menuLayout.setVisible(false);
				chatLayout.setVisible(true);
				stage.setKeyboardFocus(chatField);
				joinServer(addressText.getText(), nameText.getText());
				System.out.println("Joining chat" + addressText.getText());
			}
		});


		//MENU
		menuLayout = new Table();
		//table.debug(); // turn on all debug lines (table, cell, and widget)
		//table.debugTable(); // turn on only table lines

		menuLayout.setFillParent(true);
		menuLayout.add(nameLabel);
		menuLayout.add(nameText).width(150);
		menuLayout.row();
		menuLayout.add(addressLabel);
		menuLayout.add(addressText).width(150);
		menuLayout.row();
		menuLayout.add(playButton).width(100).padTop(20);
		menuLayout.add(chatButton).width(100).padTop(20);

		//CHAT
		chatLayout = new Table();
		Table subChatLayout = new Table();
		//chatLayout.debug(); // turn on all debug lines (table, cell, and widget)
		//chatLayout.debugTable(); // turn on only table lines

		chatLayout.setFillParent(true);
		nameList = new List<>(skin);
		chatArea = new TextArea("", skin);
		chatField = new TextField("", skin);
		chatArea.setDisabled(true);

		chatField.addListener(new InputListener() {
			@Override
			public boolean keyDown(InputEvent event, int keycode) {
				if (keycode == Input.Keys.ENTER) {
					//If message was no command we send a normal chat message
					if (!parseCommand(chatField.getText())) {
						addChatLine("Invalid command");
					}
					chatField.setText("");
				}
				return true;
			}
		});

		subChatLayout.add(chatArea).expand().fill();
		subChatLayout.row();
		subChatLayout.add(chatField).expandX().fillX();
		chatLayout.add(subChatLayout).expand().fill();
		chatLayout.add(nameList).width(150).top();
		chatLayout.setVisible(false);

		//Game GUI
		inGameLayout = new Table();
		inGameLayout.setFillParent(true);
		//inGameLayout.debug();
		gameChatField = new TextField("",skin);
		gameChatField.setVisible(false);
		gameChatField.addListener(new InputListener() {
			@Override
			public boolean keyDown(InputEvent event, int keycode) {
				if (keycode == Input.Keys.ENTER) {
					//Give input focus back to game
					Gdx.input.setInputProcessor(GameClient.this);
					gameChatField.setVisible(false);
					if (!parseCommand(gameChatField.getText())) {
						addChatLine("Invalid command");
					}
					gameChatField.setText("");
				}
				return true;
			}
		});
		messageWindow = new TextArea("" , skin);
		messageWindow.setVisible(false);

		inGameLayout.add(messageWindow).bottom().expand().fillX().height(200);
		inGameLayout.row();
		inGameLayout.add(gameChatField).bottom().expandX().fillX();
		inGameLayout.setVisible(false);


		stage.addActor(chatLayout);
		stage.addActor(menuLayout);
		stage.addActor(inGameLayout);

		stage.setKeyboardFocus(nameText);
	}

	public void showIngameMessage(String message){
		if(message != null){
			messageWindow.setVisible(true);
			messageWindow.setText(message);
		}else{
			messageWindow.setVisible(false);
			messageWindow.setText("");
		}

	}

	private void showChatBubble(final long networkID, String message){
		if(gdxWorldData == null) return;
		Entity talker = gdxWorldData.getEntityWithID(networkID);
		if(talker == null) return;
		//We create new chat bubble if one doesnt exist
		if(chatBubbles.get(networkID) == null){
			TextField chatBubble = new TextField("",skin);
			chatBubble.setWidth(100);
			chatBubble.setVisible(false);
			chatBubbles.put(networkID, chatBubble);
			stage.addActor(chatBubble);
			System.out.println("Created chat bubble for: "+Mappers.nameM.get(talker).name);
		}
		chatBubbles.get(networkID).setText(message);
		float textWidth = skin.getFont("default-font").getBounds(message).width;
		chatBubbles.get(networkID).setWidth(textWidth+10);
		chatBubbles.get(networkID).setVisible(true);

		Position pos = Mappers.positionM.get(talker);
		Bounds bounds = Mappers.boundsM.get(talker);

		//CHAT BUBBLE POSITION
		float bubbleX = pos.x + bounds.width/2 - chatBubbles.get(networkID).getWidth()/2;
		float bubbleY = pos.y + bounds.height/2 + 30;
		//FIXME find out what camera projection does
		Vector3 bubbleProjected = camera.project(new Vector3(bubbleX, bubbleY, 0));
		chatBubbles.get(networkID).setPosition(bubbleProjected.x,bubbleProjected.y);


		TimerTask chatBubbleFade = new TimerTask() {
			@Override
			public void run() {
				chatBubbles.get(networkID).setVisible(false);
				System.out.println(chatBubbles.get(networkID));
				System.out.println(networkID+":ChatbubbleFade ended");
			}
		};
		java.util.Timer timer = new java.util.Timer(true);
		timer.schedule(chatBubbleFade,5*1000);
	}

	private void joinServer(String host, String name){
		joinServer(host,name,null);
	}

	private void joinServer(final String host, final String name, final String spawnMap){
		client = new Client(writeBufferSize,objectBufferSize);
		Network.register(client);

		client.addListener(new Listener() {

			public void connected (Connection connection) {

				Register register = new Register();
				register.connectionName = name;
				client.sendTCP(register);


				Message m = new Message();
				m.text = "sync";
				client.sendTCP(m);
			}

			public void received(Connection connection, Object object) {
				if (object instanceof Message) {
					Message message = (Message)object;
					System.out.println(message.text);
					System.out.println("Received message: " + message.text + " from "+message.senderName);
					addChatLine(message.senderName+": "+message.text);
					showChatBubble(message.networkID,message.text);
				}else if (object instanceof SyncPlayerList) {
					SyncPlayerList syncPlayerList = (SyncPlayerList)object;
					nameList.setItems(syncPlayerList.playerList.toArray(new String[syncPlayerList.playerList.size()]));
				}else if (object instanceof SyncEntities) {
					SyncEntities status = (SyncEntities)object;
					//FIXME temporarily store update untill we can update entities in game loop, this fixes a lot of nullpointer exceptions but maybe there is a nicer solution
					pendingEntitySync = status.entities;
				}else if (object instanceof GoToMap){
					GoToMap goToMap = (GoToMap) object;
					//If client gets this packet it means the server allows them to change map
					//Map can't be loaded from the network thread so we set a flag that loads it when possible avoids  RuntimeException: No OpenGL context found in the current thread.
					pendingMapChange = goToMap.mapName;
					if(goToMap.destinationObject != null){
						Position pos = Mappers.positionM.get(player);
						System.out.println("Going to "+goToMap.destinationObject+"("+goToMap.x+","+goToMap.y+") in map "+goToMap.mapName);
						pos.x = goToMap.x;
						pos.y = goToMap.y;
						camera.position.set(pos.x, pos.y, 0);
						gdxWorldData.getSystem(PlayerControlSystem.class).setProcessing(true);
					}
				}else if(object instanceof Spawn){
					Spawn spawn = (Spawn) object;
					if(spawn.networkID < 0){
						//Negative networkID on spawn means server has removed this player
						pendingReset = true;
					}else{
						//Server confirms that we have spawned
						pendingMapChange = spawn.mapName;
						playerNetworkID = spawn.networkID;
						camera.position.set(spawn.x, spawn.y, 0);
					}
				}
			}
		});

		client.start();

		// We'll do the connect on a new thread so the ChatFrame can show a progress bar.
		// Connecting to localhost is usually so fast you won't see the progress bar.
		new Thread("Connect") {
			public void run() {
				try {
					client.connect(5000, host, Network.portTCP, Network.portUDP);
					// Server communication after connection can go here, or in Listener#connected().
					if(spawnMap != null){
						Spawn spawn = new Spawn();
						spawn.name = name;
						spawn.mapName = spawnMap;
						client.sendTCP(spawn);
					}
				} catch (IOException ex) {
					ex.printStackTrace();
					System.exit(1);
				}
			}
		}.start();

	}

	private void reset(){
		player = null;
		playerNetworkID = -1;
		gdxWorldData = null;
		tiledMapRenderer = null;
		pendingEntitySync = null;
		pendingMapChange = null;
		pendingReset = false;
		chatBubbles.clear();
	}

	private void loadMap(String mapName){

		gdxWorldData = GdxWorldLoader.loadWorld(mapName);
		/*If this is a mapchange we already have a player entity from server
		Loading the world like this clears all entities and adds entities from the map
		Since the player is not in the mapfile we would have to wait for the server
		to send it in a sync update and then give it the required client only components
		Instead we just add the old player entity reference this class has*/
		if(player != null){
			gdxWorldData.addEntity(player);
		}
		tiledMapRenderer = new CustomTiledMapRenderer(gdxWorldData.getActiveMap());
		initializeSystems();

		MapProperties mapProperties = gdxWorldData.getActiveMap().getProperties();
		Iterator<String> keys = mapProperties.getKeys();
		while(keys.hasNext()){
			String key = keys.next();
			System.out.println(key + ": " + mapProperties.get(key));
		}
	}

	private void initializeSystems(){

		PlayerControlSystem playerControlSystem = new PlayerControlSystem(Family.all(PlayerControlled.class).get(),gdxWorldData,this);
		gdxWorldData.addFamilyListener(Family.all(PlayerControlled.class).get(),playerControlSystem);
		gdxWorldData.addSystem(playerControlSystem);

		MapCollisionSystem mapCollisionSystem = new MapCollisionSystem(Family.all(PlayerControlled.class).get(),gdxWorldData);
		gdxWorldData.addFamilyListener(Family.all(PlayerControlled.class).get(),mapCollisionSystem);
		gdxWorldData.addSystem(mapCollisionSystem);

		MapObjectCollisionSystem mapObjectCollisionSystem = new MapObjectCollisionSystem(Family.all(PlayerControlled.class).get(),gdxWorldData,client);
		gdxWorldData.addFamilyListener(Family.all(PlayerControlled.class).get(),mapObjectCollisionSystem);
		gdxWorldData.addSystem(mapObjectCollisionSystem);

		EntityCollisionSystem entityCollisionSystem = new EntityCollisionSystem(Family.all(PlayerControlled.class).get(),gdxWorldData);
		gdxWorldData.addFamilyListener(Family.all(PlayerControlled.class).get(),entityCollisionSystem);
		gdxWorldData.addSystem(entityCollisionSystem);

		MovementApplyingSystem movementApplyingSystem = new MovementApplyingSystem(Family.all(Movement.class).get());
		gdxWorldData.addFamilyListener(Family.all(Movement.class).get(),movementApplyingSystem);
		gdxWorldData.addSystem(movementApplyingSystem);

		CameraFocusSystem cameraFocusSystem = new CameraFocusSystem(Family.all(PlayerControlled.class).get(), camera);
		gdxWorldData.addFamilyListener(Family.all(PlayerControlled.class).get(),cameraFocusSystem);
		gdxWorldData.addSystem(cameraFocusSystem);

		//Doesnt process entities no listener needed
		gdxWorldData.addSystem(new BottomMapRenderSystem(tiledMapRenderer,camera,gdxWorldData));

		ShapeRenderingSystem shapeRenderingSystem = new ShapeRenderingSystem(Family.all(Position.class).exclude(Sprite.class,AnimatedSprite.class,TileID.class).get(), shapeRenderer,camera);
		gdxWorldData.addFamilyListener(Family.all(Position.class).exclude(Sprite.class,AnimatedSprite.class,TileID.class).get(),shapeRenderingSystem);
		gdxWorldData.addSystem(shapeRenderingSystem);

		TextureRenderingSystem textureRenderingSystem = new TextureRenderingSystem(Family.one(Sprite.class,AnimatedSprite.class).get(), tiledMapRenderer.getBatch());
		gdxWorldData.addFamilyListener(Family.one(Sprite.class, AnimatedSprite.class).get(), textureRenderingSystem);
		gdxWorldData.addSystem(textureRenderingSystem);

		//Doesnt process entities no listener needed
		gdxWorldData.addSystem(new TopMapRenderSystem(tiledMapRenderer,camera,gdxWorldData));

		TileIDTextureLoadingSystem tileIDTextureLoadingSystem = new TileIDTextureLoadingSystem(Family.all(TileID.class).get(),gdxWorldData);
		gdxWorldData.addFamilyListener(Family.all(TileID.class).get(),tileIDTextureLoadingSystem);
		gdxWorldData.addSystem(tileIDTextureLoadingSystem);

		UpdateEntityOnServerSystem updateEntityOnServerSystem = new UpdateEntityOnServerSystem(Family.all(Movement.class).get(),client);
		gdxWorldData.addFamilyListener(Family.all(Movement.class).get(),updateEntityOnServerSystem);
		gdxWorldData.addSystem(updateEntityOnServerSystem);

		//Doesnt process entities no listener needed
		gdxWorldData.addSystem(new TimedSystem(1,gdxWorldData,this));
	}

	private void addChatLine(String line){
		chatArea.setText(chatArea.getText() + "\n" + line);
	}

	private boolean parseCommand(String input){

		if(input.length() == 0){
			return true;
		}

		//If input is not a command we send it as a message to server
		if(input.charAt(0) != '!'){
			Message message = new Message();
			message.text = input;
			message.networkID = playerNetworkID;
			client.sendTCP(message);
			System.out.println("Sent message: " + message.text);
			return true;
			//Parse client commands
		}else{

			String cleanCommand = input.substring(1);
			Scanner scn = new Scanner(cleanCommand);
			boolean commandParsed = false;
			String command = scn.next();

			if(command.equals("spawn")){
				commandParsed = true;
				try{
					//Request a spawn from server, server might change spawn location
					Spawn spawn = new Spawn();
					spawn.name = scn.next();
					spawn.mapName = scn.next();
					spawn.x = 0;
					spawn.y = 0;
					client.sendTCP(spawn);
				}catch(InputMismatchException e) {
					addChatLine("arguments need to be strings");
				}catch(NoSuchElementException e){
					addChatLine("spawn command needs 2 arguments");
				}
			}else if(command.equals("map")){
				commandParsed = true;
				try{
					String mapName = scn.next();
					GoToMap goToMap = new GoToMap();
					goToMap.mapName = mapName;
					addChatLine("Moving to " + mapName);
					client.sendTCP(goToMap);
				}catch(InputMismatchException e){
					addChatLine("argument need to be string");
				}catch(NoSuchElementException e){
					addChatLine("map command needs 1 argument");
				}

			}else if(command.equals("togglesystem")){
				commandParsed = true;
				try{
					int index = scn.nextInt();
					gdxWorldData.toggleSystem(index);
				}catch(InputMismatchException e){
					addChatLine("argument needs to be integer");
				}catch(NoSuchElementException e){
					addChatLine("togglesystem command needs 1 argument");
				}
			}else if(command.equals("toggleallsystems")){
				commandParsed = true;
				try{
					boolean enabled = scn.nextBoolean();
					gdxWorldData.toggleAllSystems(enabled);
				}catch(InputMismatchException e){
					addChatLine("argument 2 needs to be integer");
				}catch(NoSuchElementException e){
					addChatLine("togglesystem command needs 2 arguments");
				}
			}else if(command.equals("chat")){
				commandParsed = true;
				try{
					boolean showChat = scn.nextBoolean();
					if(showChat){
						chatLayout.setVisible(true);
						inGameLayout.setVisible(false);
						Gdx.input.setInputProcessor(stage);
						stage.setKeyboardFocus(chatField);
					}else{
						inGameLayout.setVisible(true);
						chatLayout.setVisible(false);
						Gdx.input.setInputProcessor(GameClient.this);
					}

				}catch(InputMismatchException e){
					addChatLine("argument need to be boolean");
				}catch(NoSuchElementException e){
					addChatLine("chat command needs 1 argument");
				}

			}
			scn.close();
			return commandParsed;
		}
	}

	@Override
	public void render () {

		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		tickStartTime = System.nanoTime();

		if(pendingReset){
			reset();
		}

		if(pendingMapChange != null){
			loadMap(pendingMapChange);
			pendingMapChange = null;
		}

		mapChangeDuration = System.nanoTime()-tickStartTime;

		//Sync entities with server
		if(pendingEntitySync != null){
			gdxWorldData.updateEntities(pendingEntitySync);
			pendingEntitySync = null;
		}

		syncDuration = System.nanoTime()-mapChangeDuration-tickStartTime;

		//This should only run once after first spawn when server sends the player in a sync update
		if(playerNetworkID >= 0 && player == null){
			player = gdxWorldData.getEntityWithID(playerNetworkID);
			if(player == null){
				return;
			}
			player.add(new AnimatedSprite(new TextureAtlas(Gdx.files.internal("data/spritesheetindexed.atlas"))));
			player.add(new PlayerControlled());
		}


		if(tiledMapRenderer != null){
			float delta = Gdx.graphics.getDeltaTime(); //seconds
			gdxWorldData.updateWorld(delta);
		}

		updateDuration = System.nanoTime()-syncDuration-mapChangeDuration-tickStartTime;

		//Keep chat bubbles over players
		if(!chatBubbles.isEmpty()){
			for(long id : chatBubbles.keySet()){
				TextField chatBubble = chatBubbles.get(id);
				Entity e = gdxWorldData.getEntityWithID(id);
				if(e==null){
					chatBubbles.remove(id);
					continue;
				}
				Position pos = Mappers.positionM.get(e);
				Bounds bounds = Mappers.boundsM.get(e);

				float bubbleX = pos.x + bounds.width/2 - chatBubbles.get(id).getWidth()/2;
				float bubbleY = pos.y + bounds.height/2 + 30;
				//FIXME find out what camera projection does
				Vector3 bubbleProjected = camera.project(new Vector3(bubbleX, bubbleY, 0));
				chatBubbles.get(id).setPosition(bubbleProjected.x, bubbleProjected.y);
			}
		}

		//Draw GUI
		stage.draw();
		if(client != null){
			Gdx.graphics.setTitle("FPS:" + Gdx.graphics.getFramesPerSecond() + " Ping:"+client.getReturnTripTime()+"ms");
		}else{
			Gdx.graphics.setTitle("FPS:" + Gdx.graphics.getFramesPerSecond());
		}

		guiUpdateDuration = System.nanoTime()-updateDuration-syncDuration-mapChangeDuration-tickStartTime;
	}

	@Override
	public void dispose() {
		batch.dispose();
	}

	@Override
	public boolean keyDown(int keycode) {

		if(keycode == Input.Keys.LEFT){
			left = true;
		}
		if(keycode == Input.Keys.RIGHT){
			right = true;
		}
		if(keycode == Input.Keys.UP){
			up = true;
		}
		if(keycode == Input.Keys.DOWN){
			down = true;
		}

		if(keycode == Input.Keys.ENTER){
			if(gameChatField.isVisible()){
				//This should never happen unless gamechat is visible when game starts
				gameChatField.setVisible(false);
			}else{
				gameChatField.setVisible(true);
				stage.setKeyboardFocus(gameChatField);
				Gdx.input.setInputProcessor(stage);
			}
		}

		return false;
	}

	@Override
	public boolean keyUp(int keycode) {

		if(keycode == Input.Keys.LEFT){
			left = false;
		}

		if(keycode == Input.Keys.RIGHT){
			right = false;
		}

		if(keycode == Input.Keys.UP){
			up = false;
		}

		if(keycode == Input.Keys.DOWN){
			down = false;
		}

		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		return false;
	}
	@Override
	public void resize(int width, int height) {

	}

	@Override
	public void pause() {
		System.out.println("PAUSED");
	}

	@Override
	public void resume() {
		System.out.println("RESUMED");

	}

}
