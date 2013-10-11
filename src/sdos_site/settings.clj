(ns sdos-site.settings)

(def redirect-home
  {:status 302
   :headers {"Location" "/"}
   :body ""})

(def redirect-to-login
  {:status 302
   :headers {"Location" "/user/login"}
   :body ""})

(def base-url
  "http://dos.sauerworld.org")

(def links
  [["Home" "/"]
   ["About" "/about"]
   ["Events" "/events"]
   ;;["Results" "/results"]
   ])

(def user-links
  [["Sign Up For SDoS" "/user/signup"]
   ["Profile" "/user/"]
   ["Logout" "/user/logout"]
   ["Generate Auth Key" "/user/authkey"]])

(def admin-links
  [["View/Edit Articles" "/admin/"]
   ["Add Article" "/admin/articles/create"]])

(def login-link
  ["Log in" "/user/login"])

(def registration-link
  ["Register" "/user/register"])

(def layout-settings
  {:email-title "Interested?"
   :email-subtitle "Sign up for email updates!"
   :menu-title "Menu"
   :user-menu-title "User"
   :admin-menu-title "Admin"
   :menu-items links
   :user-menu-items user-links
   :admin-menu-items admin-links})

;; todo - have this function handle the modal login/logout and presence of
;; registration link depending on login status
(defn get-settings [req]
  (let [logged-in? (not (nil? (-> req :session :user)))
        admin? (true? (-> req :session :user :admin))
        menu-items (if logged-in?
                     links
                     ;;(conj links login-link registration-link) ;; put back later
                     links
                     )]
    (merge layout-settings {:logged-in logged-in?
                            :admin admin?
                            :menu-items menu-items})))
