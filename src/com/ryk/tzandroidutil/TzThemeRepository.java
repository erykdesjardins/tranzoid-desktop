package com.ryk.tzandroidutil;

import java.util.HashMap;

public class TzThemeRepository {
	private static TzThemeRepository _this = new TzThemeRepository();
	private TzTheme theme;
	
	private TzThemeRepository() {
		
	}
	
	public static TzThemeRepository getRepo() {
		return _this;
	}
	
	public TzTheme getTheme() {
		return theme;
	}
	
	public void setTheme(String name) {
		theme = new TzTheme(name);
	}
	
	public class TzTheme {
		HashMap<String, String> values = new HashMap<String, String>();
		
		public TzTheme(String type) {
			if (type.equals("blurrysky")) {
				values.put("color1", "#606c88");
				values.put("color2", "#3f4c6b");
				values.put("bar1", "#aebcbf");
				values.put("bar2", "#6ebf74");
				values.put("light1", "#b0d4e3");
				values.put("light2", "#88bacf");
				
				values.put("loading", "rgba(96,108,136,0.9)");
				
				values.put("hoverColor", "rgba(0, 0, 0, 0.5)");
				values.put("fontColor", "#FFF");
				values.put("subfontColor", "#EEE");
				
				values.put("messageIn", "rgba(47, 0, 79, 0.7)");
				values.put("messageOut", "rgba(0, 10, 81, 0.7)");

				values.put("wallpaper", "../mediaimg/centerbg.png");
			} else if (type.equals("candy")) {
				values.put("color1", "#de47ac");
				values.put("color2", "#ad1283");
				values.put("bar1", "#aebcbf");
				values.put("bar2", "#6ebf74");
				values.put("light1", "#cc99cc");
				values.put("light2", "#eeccee");
				
				values.put("loading", "rgba(150, 50, 150, 0.9)");
				
				values.put("hoverColor", "rgba(0, 0, 0, 0.5)");
				values.put("fontColor", "#FFF");
				values.put("subfontColor", "#EEE");
				
				values.put("messageIn", "rgba(100, 0, 100, 0.7)");
				values.put("messageOut", "rgba(180, 0, 160, 0.7)");	

				values.put("wallpaper", "http://www.travelsworlds.com/wp-content/uploads/2013/12/pink-snowflake-wallpaperdownload-wallpaper-pink-snowflakes-frosty-uzzory-free-desktop-45m6dvro.jpg");
			} else if (type.equals("royalred")) {
				values.put("color1", "#a90329");
				values.put("color2", "#6d0019");
				values.put("bar1", "#aebcbf");
				values.put("bar2", "#6ebf74");
				values.put("light1", "#cc9999");
				values.put("light2", "#eecccc");
				
				values.put("loading", "rgba(150, 50, 50, 0.9)");
				
				values.put("hoverColor", "rgba(0, 0, 0, 0.5)");
				values.put("fontColor", "#FFF");
				values.put("subfontColor", "#EEE");
				
				values.put("messageIn", "rgba(90, 0, 40, 0.7)");
				values.put("messageOut", "rgba(170, 0, 40, 0.7)");	

				values.put("wallpaper", "http://tranzoid.com/app/wp/Tranzoid-Wine.jpg");
			} else if (type.equals("forest")) {
				values.put("color1", "#007729");
				values.put("color2", "#005121");
				values.put("bar1", "#aebcbf");
				values.put("bar2", "#6ebf74");
				values.put("light1", "#99cc99");
				values.put("light2", "#cceecc");
				
				values.put("loading", "rgba(50, 150, 50, 0.9)");
				
				values.put("hoverColor", "rgba(0, 0, 0, 0.5)");
				values.put("fontColor", "#FFF");
				values.put("subfontColor", "#EEE");
				
				values.put("messageIn", "rgba(0, 120, 40, 0.7)");
				values.put("messageOut", "rgba(0, 40, 0, 0.7)");	
				
				values.put("wallpaper", "http://miriadna.com/desctopwalls/images/max/Fairy-forest.jpg");
			} else if (type.equals("konga")) {
				values.put("color1", "#8c3310");
				values.put("color2", "#752201");
				values.put("bar1", "#aebcbf");
				values.put("bar2", "#6ebf74");
				values.put("light1", "#996633");
				values.put("light2", "#AA9966");
				
				values.put("loading", "rgba(80, 20, 20, 0.9)");
				
				values.put("hoverColor", "rgba(0, 0, 0, 0.5)");
				values.put("fontColor", "#FFF");
				values.put("subfontColor", "#EEE");
				
				values.put("messageIn", "rgba(80, 20, 0, 0.7)");
				values.put("messageOut", "rgba(122, 37, 0, 0.7)");	

				values.put("wallpaper", "http://tranzoid.com/app/wp/Tranzoid-Bongo.jpg");
			}  else if (type.equals("morning")) {
				values.put("color1", "#499bea");
				values.put("color2", "#207ce5");
				values.put("bar1", "#aebcbf");
				values.put("bar2", "#6ebf74");
				values.put("light1", "#9999cc");
				values.put("light2", "#ccccee");
				
				values.put("loading", "rgba(100, 100, 200, 0.9)");
				
				values.put("hoverColor", "rgba(0, 0, 0, 0.5)");
				values.put("fontColor", "#FFF");
				values.put("subfontColor", "#EEE");
				
				values.put("messageIn", "rgba(10, 10, 100, 0.7)");
				values.put("messageOut", "rgba(40, 60, 100, 0.7)");	
				
				values.put("wallpaper", "http://tranzoid.com/app/wp/Tranzoid-Beach.jpg");
			} else {
				values.put("color1", "#606c88");
				values.put("color2", "#3f4c6b");
				values.put("bar1", "#aebcbf");
				values.put("bar2", "#6ebf74");
				values.put("light1", "#b0d4e3");
				values.put("light2", "#88bacf");
				
				values.put("loading", "rgba(96,108,136,0.9)");
				
				values.put("hoverColor", "rgba(0, 0, 0, 0.5)");
				values.put("fontColor", "#FFF");
				values.put("subfontColor", "#EEE");
				
				values.put("messageIn", "rgba(47, 0, 79, 0.7)");
				values.put("messageOut", "rgba(0, 10, 81, 0.7)");	

				values.put("wallpaper", "../mediaimg/centerbg.png");			
			}
		}
		
		public HashMap<String, String> getMap() {
			return values;
		}
		
		public String getValue(String field) {
			String value = values.get(field);
			return value == null ? "" : value;
		}
	}
}
