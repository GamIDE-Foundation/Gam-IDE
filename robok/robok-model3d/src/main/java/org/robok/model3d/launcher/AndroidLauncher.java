package org.robok.model3d.launcher;

/*
 *  This file is part of Robok © 2024.
 *
 *  Robok is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Robok is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *   along with Robok.  If not, see <https://www.gnu.org/licenses/>.
 */ 

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;

import org.robok.model3d.databinding.Activity3dModelBinding;
import org.robok.model3d.objects.SceneObject;
import org.robok.model3d.view.Model3DView;

import java.util.ArrayList;
import java.util.List;

public class AndroidLauncher extends AndroidApplication {
    
    Activity3dModelBinding binding;
    
    @Override
    protected void onDestroy() {
        binding = null;
        super.onDestroy();
    }
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        binding = Activity3dModelBinding.inflate(getLayoutInflater());
        
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        View libgdxView = initializeForView(new Model3DView(), config);
        Model3DView model3dView = Model3DView.clazz;
        
      //  My3dGame.game.setListObjects(sceneObjects);
       binding.criarCubo.setOnClickListener(v ->{
            model3dView.setCommand("createCube");                
        });
        
        binding.criarTriangulo.setOnClickListener(v ->{
            model3dView.setCommand("createTriangle");
        });
        
        binding.criarEsfera.setOnClickListener(v ->{
            model3dView.setCommand("createSphere");
        });
        
        binding.criarCilindro.setOnClickListener(v ->{
            model3dView.setCommand("createCylinder");
        });
        
        binding.criarCone.setOnClickListener(v ->{
            model3dView.setCommand("createCone");
        });
        
        binding.criarPlano.setOnClickListener(v ->{
            model3dView.setCommand("createPlane");
        });
        
        binding.linearLibGdx.addView(libgdxView);
        setContentView(binding.getRoot());
        hideSystemUI();
    }
    
    private void hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
    getWindow().setDecorFitsSystemWindows(false);

    if (getWindow().getInsetsController() != null) {
        getWindow().getInsetsController().hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
        getWindow().getInsetsController().setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
    }
} else {
    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
}
    }
}
