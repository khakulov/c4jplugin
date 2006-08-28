

/**********************************************************************************************

                               Customizable Options and Styles

**********************************************************************************************/


function tmenudata0()
{

    /*---------------------------------------------
    Animation Settings
    ---------------------------------------------*/


	this.animation_jump = 10		//Measured in Milliseconds (1/1000s)
	this.animation_delay = 5		//Measured in pixels

	

    /*---------------------------------------------
    Image Settinngs (icons and plus minus symbols)
    ---------------------------------------------*/


	this.imgage_gap = 3				//The image gap is applied to the left and right of the folder and document icons.
							//In the absence of a folder or document icon the gap is applied between the 
							//plus / minus symbols and the text only.

	
	this.plus_image = "images/plus.gif"		//specifies a custom plus image.
	this.minus_image = "images/minus.gif"		//specifies a custom minus image.
	this.pm_width_height = "9,9"			//Width & Height  - Note: Both images must be the same dimensions.


	this.folder_image = "images/folder.gif"		//Automatically applies to all items which may be expanded.
	this.document_image = "images/document.gif"	//Automatically applies to all items which are not expandable.
	this.icon_width_height = "16,14"		//Width & Height  - Note: Both images must be the same dimensions.




    /*---------------------------------------------
    General Settings
    ---------------------------------------------*/


	this.indent = 20;			//The indent distance in pixels for each level of the tree.
	this.use_hand_cursor = true;		//Use a hand mouse cursor for expandable items, or the default arrow.




    /*---------------------------------------------
    Tree Menu Styles
    ---------------------------------------------*/


	this.main_item_styles =           "text-decoration:none;		\
                                           font-weight:normal;			\
                                           font-family:Arial;			\
                                           font-size:12px;			\
                                           color:#333333;			\
                                           padding:2px;				"
                                           
                                          
        this.sub_item_styles =            "text-decoration:none;		\
                                           font-weight:normal;			\
                                           font-family:Arial;			\
                                           font-size:12px;			\
                                           color:#333333;			"			



	/* Styles may be formatted as multi-line (seen above), or on a single line as shown below.
	   The expander_hover_styles apply to menu items which expand to show child menus.*/
		


	this.main_container_styles = "padding:0px;"
	this.sub_container_styles = "padding-top:5px; padding-bottom:5px;"

	this.main_link_styles = "color:#0066aa; text-decoration:none;"
	this.main_link_hover_styles = "color:#c27d06; text-decoration:underline;"
	
	this.sub_link_styles = ""
	this.sub_link_hover_styles = ""
	
	this.main_expander_hover_styles = "text-decoration:underline;";
	this.sub_expander_hover_styles = "";
	

}

