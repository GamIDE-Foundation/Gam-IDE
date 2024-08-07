package org.gampiot.robokide.feature.template.code.rbk;

import android.os.Parcel;

import org.gampiot.robokide.feature.template.code.CodeTemplate;

public class RBKLayoutTemplate extends CodeTemplate {

     public RBKLayoutTemplate() {}

     public RBKLayoutTemplate(Parcel in) {
          super(in);
     }
     
     @Override
     public String getName() {
          return "RBK Layout File";
     }
     
     @Override
     public void configure() {
          setContent(
               CodeTemplate.CLASS_NAME
               + " {" 
               + "     Button(" 
               + "        id = \"shoot_button\"," 
               + "        text = \"Shoot\"," 
               + "        width = px(20)," 
               + "        height = px(20)" 
               + "     )" 
               + "}"
          );
     }
     
     @Override
     public String getExtension() {
          return ".rbk";
     }
}
