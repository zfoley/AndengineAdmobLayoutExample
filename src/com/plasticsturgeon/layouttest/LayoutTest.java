package com.plasticsturgeon.layouttest;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.handler.IUpdateHandler;
import org.anddev.andengine.engine.handler.physics.PhysicsHandler;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.background.ColorBackground;
import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.entity.text.Text;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.opengl.font.Font;
import org.anddev.andengine.opengl.texture.Texture;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.anddev.andengine.sensor.accelerometer.AccelerometerData;
import org.anddev.andengine.sensor.accelerometer.IAccelerometerListener;
import org.anddev.andengine.ui.activity.LayoutGameActivity;
import org.anddev.andengine.util.HorizontalAlign;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;


import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.os.Message;

public class LayoutTest extends LayoutGameActivity implements IAccelerometerListener{
	private static final int CAMERA_WIDTH = 720;
	private static final int CAMERA_HEIGHT = 480;

	private static final float DEMO_VELOCITY = 100.0f;
	private Camera mCamera;
	private Engine mEngine;
	private Texture mTexture;
	private TiledTextureRegion mFaceTextureRegion;
	private TextureRegion mBossTextureRegion;
	private BossSprite boss;
	private Scene scene;
	private Font mFont;
	private Texture mFontTexture;
	
	 @Override
     protected int getLayoutID() {
             return R.layout.main;
     }

     @Override
     protected int getRenderSurfaceViewID() {
             return R.id.xmllayoutexample_rendersurfaceview;
     }
	
	@Override
	public Engine onLoadEngine() {
		mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		mEngine = new Engine(new EngineOptions(true,ScreenOrientation.LANDSCAPE,new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT),mCamera));
//		AdView adView = (AdView)this.findViewById(R.id.adView);
//	    adView.loadAd(new AdRequest());
		return mEngine;
	}

	@Override
	public void onLoadResources() {
		this.mTexture = new Texture(512, 128, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.mFaceTextureRegion = TextureRegionFactory.createTiledFromAsset(this.mTexture, this, "ass_big_tran.png", 0, 0, 8, 2);
		this.mBossTextureRegion = TextureRegionFactory.extractFromTexture(mTexture, 64, 0, 64, 64);
		this.mEngine.getTextureManager().loadTexture(this.mTexture);
		this.enableAccelerometerSensor(this);
		
		this.mFontTexture = new Texture(256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);

		this.mFont = new Font(this.mFontTexture, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 32, true, Color.BLACK);

		this.mEngine.getTextureManager().loadTexture(this.mFontTexture);
		this.mEngine.getFontManager().loadFont(this.mFont);
	}

	@Override
	public Scene onLoadScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());

		scene = new Scene(3);
		scene.setBackground(new ColorBackground(0.3f, 0.3f, 0.3f));

		final int centerX = (CAMERA_WIDTH - this.mFaceTextureRegion.getWidth()) / 2;
		final int centerY = (CAMERA_HEIGHT - this.mFaceTextureRegion.getHeight()) / 2;
		ball = new Ball(centerX, centerY, this.mFaceTextureRegion);
		ball.mPhysicsHandler.setVelocity(DEMO_VELOCITY, -DEMO_VELOCITY);
		boss = new BossSprite(centerX, centerY, this.mBossTextureRegion);
		boss.mPhysicsHandler.setVelocity(-DEMO_VELOCITY, -DEMO_VELOCITY);
		textRight = new Text(400, 240, this.mFont, "+25", HorizontalAlign.RIGHT);

		scene.attachChild(textRight);


		scene.attachChild(ball);
		scene.attachChild(boss);
		
		scene.registerUpdateHandler(new IUpdateHandler() {
			@Override
			public void onUpdate(float pSecondsElapsed) {

			        if(boss.collidesWith(ball)){
			        	float collisionAngle = (float) rotateTowards(ball.getX(), ball.getY(), boss.getX(), boss.getY());
			        	PointF reaction = projectPointAlongAngle(-collisionAngle, 200);
			        	ball.mPhysicsHandler.setVelocity(reaction.x,-reaction.y);
			        	boss.mPhysicsHandler.setVelocity(-reaction.x,reaction.y);
			        }
			}

			@Override
			public void reset() {
				
			}
		});
		Message m = new Message();
		m.what = 0;
		return scene;
	}

	private Ball ball;
	private Text textRight;
	private boolean enableAds = true;
		
	@Override
	public void onAccelerometerChanged(AccelerometerData pAccelerometerData) {
						
		boss.mPhysicsHandler.setAcceleration(pAccelerometerData.getY()*60, pAccelerometerData.getX()*60);
	}
	/**
	 * Indicates the heading from point x1,y1 to point x2,y2
	 * 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return the angle in radians
	 */
	public double rotateTowards(double x1, double y1, double x2, double y2) {
		double dx = x1 - x2;
		double dy = y1 - y2;
		double radianAngle = Math.atan2(dy, dx);
		return radianAngle;
	}

	/**
	 * 
	 * @param angle
	 *            measured in radians
	 * @param distance
	 * @return PointF of the offset.
	 */
	public PointF projectPointAlongAngle(double angle, double distance) {
		PointF p = new PointF();
		p.x = (float) (Math.cos(angle) * distance);
		p.y = (float) (Math.sin(angle) * distance);
		return p;
	}
	
	
	@Override
	public void onLoadComplete() {
		/* AdView adView = (AdView)this.findViewById(R.id.adView);
        adView.refreshDrawableState();
       
        if(enableAds)
        {
                adView.setVisibility(AdView.VISIBLE);
                AdRequest adRequest = new AdRequest();
                adRequest.setTesting(true);
                adView.loadAd(adRequest);
        }
		 */	
	}

	private static class Ball extends AnimatedSprite {
		final PhysicsHandler mPhysicsHandler;
		public Ball(final float pX, final float pY, final TiledTextureRegion pTextureRegion) {
			super(pX, pY, pTextureRegion);
			mPhysicsHandler = new PhysicsHandler(this);
			this.registerUpdateHandler(mPhysicsHandler);			
		}

		@Override
		protected void onManagedUpdate(final float pSecondsElapsed) {
			if(this.mX < 0) {
				this.mPhysicsHandler.setVelocityX(DEMO_VELOCITY);
			} else if(this.mX + this.getWidth() > CAMERA_WIDTH) {
				this.mPhysicsHandler.setVelocityX(-DEMO_VELOCITY);
			}

			if(this.mY < 0) {
				this.mPhysicsHandler.setVelocityY(DEMO_VELOCITY);
			} else if(this.mY + this.getHeight() > CAMERA_HEIGHT) {
				this.mPhysicsHandler.setVelocityY(-DEMO_VELOCITY);
			}

			super.onManagedUpdate(pSecondsElapsed);		}
	}
	class BossSprite extends Sprite {
		final PhysicsHandler mPhysicsHandler;
		public BossSprite(int pX, int pY, TextureRegion pTextureRegion) {
			super(pX, pY, pTextureRegion);
			mPhysicsHandler = new PhysicsHandler(this);
			this.registerUpdateHandler(mPhysicsHandler);			
		}

		@Override
		protected void onManagedUpdate(float pSecondsElapsed) {
			if(this.mX < 0) {
				this.mPhysicsHandler.setVelocityX(DEMO_VELOCITY);
			} else if(this.mX + this.getWidth() > CAMERA_WIDTH) {
				this.mPhysicsHandler.setVelocityX(-DEMO_VELOCITY);
			}

			if(this.mY < 0) {
				this.mPhysicsHandler.setVelocityY(DEMO_VELOCITY);
			} else if(this.mY + this.getHeight() > CAMERA_HEIGHT) {
				this.mPhysicsHandler.setVelocityY(-DEMO_VELOCITY);
			}

			super.onManagedUpdate(pSecondsElapsed);
		}
	}

}