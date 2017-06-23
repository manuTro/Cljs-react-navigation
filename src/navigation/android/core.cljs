(ns navigation.android.core
  (:require [reagent.core :as r :refer [atom]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [navigation.events]
            [navigation.subs]))

(def ReactNative (js/require "react-native"))
(def ReactNav (js/require "react-navigation"))
(def cardstyle (js/require "react-navigation/src/views/CardStackStyleInterpolator.js"))
(def NavigationActions (.-NavigationActions ReactNav))
(defonce Text (.-Text ReactNative))

(def app-registry (.-AppRegistry ReactNative))
(def text (r/adapt-react-class (.-Text ReactNative)))
(def view (r/adapt-react-class (.-View ReactNative)))
(def image (r/adapt-react-class (.-Image ReactNative)))
(def touchable-highlight (r/adapt-react-class (.-TouchableHighlight ReactNative)))
(def Stack-navigator (.-StackNavigator ReactNav))
(def TabNavigator (.-TabNavigator ReactNav))
(def DrawerNavigator (.-DrawerNavigator ReactNav))
(def StackRouter (.-StackRouter ReactNav))

;goback is like a pop for StackNavigator, but for TabNavigator Goes to the first tab, if not already selected

;; examples of functions to use with dispatch
(def navigateAction (.. NavigationActions (navigate #js {:routeName "Chat"}))) ;use with dispatch
(def goback (.. NavigationActions (back #js {:key "Chat"}))) ;use with dispatch
(def gotochat (.. NavigationActions (reset #js {:index 1
                                                :actions #js[(.. NavigationActions (navigate #js {:routeName "Chat"}))]})))
(def gotosec (.. NavigationActions (navigate #js {:routeName "Main" :action (.. NavigationActions (navigate #js {:routeName "Second"}))})))

(def routes (atom {}))

(defn my-screen []
             [view [touchable-highlight {:style {:background-color "#999" :padding 10 :border-radius 5}
                                         :on-press #(.navigate (get @routes "Welcome") "Second")
                                   ;#(.. nav (dispatch gotosec)) ;second way
}
                    [text {:style {:color "white" :text-align "center" :font-weight "bold"}} "Press Me"]]])

(defn second-screen []
             [view [touchable-highlight {:style {:background-color "#999" :padding 10 :border-radius 5}
                                         :on-press
                                         #(.navigate (get @routes "Main") "Chat")}
                    [text {:style {:color "white" :text-align "center" :font-weight "bold"}} "Second"]]])

(defn chat-screen []
             [view [touchable-highlight {:style {:background-color "#999" :padding 10 :border-radius 5}
                                         :on-press  #(.navigate (get @routes "chat") "Prova")}
                    [text {:style {:color "white" :text-align "center" :font-weight "bold"}} "Go back"]]])


(defn prova-screen []
             [view [touchable-highlight {:style {:background-color "#999" :padding 10 :border-radius 5}
                                         :on-press #(.navigate (get @routes "Prova") "Chat")}
                    [text {:style {:color "white" :text-align "center" :font-weight "bold"}} "Prova"]]])


(defn wrapper-prova
 "Convert the reagent component into a normal react component and
 Set the navigationOptions onto the component.
 Moreover add the navigation props at the component and save it into the routes atom"
 [screen title ]
 (let [reactscreen (r/reactify-component  (fn [{:keys [navigation]}] [screen (swap! routes assoc title navigation)]) )]
 (aset reactscreen "navigationOptions" (clj->js {:title title}))
    reactscreen))


(defn main-screen []
  (r/create-element (Stack-navigator (clj->js {:MyScreen {:screen MyScreen}
                                               :Second {:screen SecondScreen}})
                                   ;(clj->js {:swipeEnabled false :animationEnabled false :navigationOptions {:tabBarVisible false}}); con il tab per far montare le componenti
                                  (clj->js  {:navigationOptions {:header null}
                                            :cardstyle {:backgroundColor "white"}
                                            :transitionConfig (fn [] #js {:screenInterpolator (.-forHorizontal (.-default cardstyle))})}) ;per lo stack per far muovere orizzontalmente
                                  ;(clj->js {:navigationOptions {:header null}})
                                  )))

(def MainScreen (wrapper-prova main-screen "Main"))
(def MyScreen (wrapper-prova my-screen "Welcome"))
(def ChatScreen (wrapper-prova chat-screen "chat"))
(def SecondScreen (wrapper-prova second-screen "Second"))
(def ProvaScreen (wrapper-prova prova-screen "Prova"))

(defn app-root []
  "  With :tabBarVisible false I decided to hide the header
    If you use StackNavigator to hide the bar use :header null"
                          (r/create-element
                            (Stack-navigator
                             (clj->js {
                                       :Main {:screen MainScreen}
                                       :Chat {:screen ChatScreen}
                                       :Prova {:screen ProvaScreen}
                                       })
                                       ))
                                       )


(defn init []
  (dispatch-sync [:initialize-db])
  (.registerComponent app-registry "navigation" #(r/reactify-component app-root)))
