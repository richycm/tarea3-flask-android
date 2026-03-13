from flask import Flask, jsonify, request
from flask_sqlalchemy import SQLAlchemy
from flask_bcrypt import Bcrypt

app = Flask(__name__)

# Configuración DB
app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///site.db'
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False

db = SQLAlchemy(app)
bcrypt = Bcrypt(app)

# Modelo Usuario
class User(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    username = db.Column(db.String(20), unique=True, nullable=False)
    password = db.Column(db.String(60), nullable=False)

    def to_dict(self):
        return {
            "id": self.id,
            "username": self.username
        }

    def __repr__(self):
        return f"User('{self.username}')"


# ----------------------------
# RUTAS
# ----------------------------

@app.route('/')
def hello():
    return jsonify({"message": "API Funcionando"})


# ----------------------------
# REGISTRO
# ----------------------------
@app.route('/register', methods=['POST'])
def register():

    data = request.get_json()

    username = data.get('username')
    password = data.get('password')
    password_confirm = data.get('password_confirm')

    # Validación
    if not username or not password or not password_confirm:
        return jsonify({"error": "username, password y password_confirm son requeridos"}), 400

    if password != password_confirm:
        return jsonify({"error": "las contraseñas no coinciden"}), 400

    if len(password) < 4:
        return jsonify({"error": "password debe tener al menos 4 caracteres"}), 400

    # Verificar usuario existente
    if User.query.filter_by(username=username).first():
        return jsonify({"message": "El usuario ya existe"}), 400

    # Hash password
    hashed_password = bcrypt.generate_password_hash(password).decode('utf-8')

    new_user = User(username=username, password=hashed_password)

    db.session.add(new_user)
    db.session.commit()

    return jsonify({"message": "Usuario creado exitosamente"}), 201


# ----------------------------
# LOGIN
# ----------------------------
@app.route('/login', methods=['POST'])
def login():

    data = request.get_json()

    username = data.get('username')
    password = data.get('password')

    user = User.query.filter_by(username=username).first()

    if user and bcrypt.check_password_hash(user.password, password):

        return jsonify({
            "status": "success",
            "message": "Login exitoso",
            "user_id": user.id,
            "username": user.username
        }), 200

    return jsonify({
        "status": "error",
        "message": "Credenciales inválidas"
    }), 401


@app.route('/users', methods=['GET'])
def get_users():

    users = User.query.all()

    return jsonify([user.to_dict() for user in users])


@app.route('/users/<int:user_id>/username', methods=['PUT'])
def update_username(user_id):

    data = request.get_json()
    new_username = data.get('username')

    if not new_username:
        return jsonify({"error": "username requerido"}), 400

    if User.query.filter_by(username=new_username).first():
        return jsonify({"error": "username ya existe"}), 400

    user = User.query.get(user_id)

    if not user:
        return jsonify({"error": "usuario no encontrado"}), 404

    user.username = new_username

    db.session.commit()

    return jsonify({
        "message": "username actualizado",
        "id": user.id,
        "username": user.username
    })


# ----------------------------
# CAMBIAR PASSWORD
# ----------------------------
@app.route('/users/<int:user_id>/password', methods=['PUT'])
def update_password(user_id):

    data = request.get_json()

    old_password = data.get('old_password')
    new_password = data.get('new_password')
    confirm_password = data.get('confirm_password')

    if not old_password or not new_password or not confirm_password:
        return jsonify({"error": "faltan campos requeridos"}), 400

    if new_password != confirm_password:
        return jsonify({"error": "las nuevas contraseñas no coinciden"}), 400

    if len(new_password) < 4:
        return jsonify({"error": "la nueva contraseña debe tener al menos 4 caracteres"}), 400

    user = User.query.get(user_id)

    if not user:
        return jsonify({"error": "usuario no encontrado"}), 404


    if not bcrypt.check_password_hash(user.password, old_password):
        return jsonify({"error": "contraseña actual incorrecta"}), 401


    hashed_password = bcrypt.generate_password_hash(new_password).decode('utf-8')

    user.password = hashed_password

    db.session.commit()

    return jsonify({"message": "contraseña actualizada correctamente"})


@app.route('/users/<int:user_id>', methods=['DELETE'])
def delete_user(user_id):

    user = User.query.get(user_id)

    if not user:
        return jsonify({"error": "usuario no encontrado"}), 404

    db.session.delete(user)
    db.session.commit()

    return jsonify({"message": "usuario eliminado"})

if __name__ == '__main__':

    with app.app_context():
        db.create_all()

    app.run(host='0.0.0.0', port=5000, debug=True)