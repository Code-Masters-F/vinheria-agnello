# Imports
import warnings
warnings.filterwarnings('ignore')

import os

os.environ.setdefault("MPLCONFIGDIR", "/tmp/matplotlib")

import pandas as pd
import seaborn as sns
import matplotlib.pyplot as plt

from sklearn.preprocessing import OneHotEncoder, MinMaxScaler
from sklearn.model_selection import train_test_split

from sklearn.neighbors import KNeighborsClassifier
from sklearn.linear_model import LogisticRegression
from sklearn.svm import SVC
from sklearn.tree import DecisionTreeClassifier
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import accuracy_score, classification_report
from sklearn.metrics import confusion_matrix

# para mostrar a tabela inteira nos prints, config do pandas.
pd.set_option("display.max_rows", None)
pd.set_option("display.max_columns", None)

# Carregar o dataset
df = pd.read_csv("data/raw/vendas_vinhos.csv")

# Exibir as primeiras linhas do DataFrame
print(df.head())

# Mostra informações gerais sobre a base
print(df.info())

'''
Mesmo não tendo valores ausente, outros problemas podem aparecer, como outliers ou dados
duplicados.
Precisa verificar mais informações relevantes à limpeza.
'''

# Verificar a presença de dados duplicados
duplicates = df.duplicated().sum()
print("Número de dados duplicados:", duplicates)

# Verificar a presença de outliers
plt.figure(figsize=(12, 6))
sns.boxplot(data=df.select_dtypes(include='number'))
plt.title("Boxplot para detectar outliers")
plt.xticks(rotation=45)
plt.show()
plt.close()

# Exploração da distribuição dos labels
plt.figure(figsize=(8, 5))
sns.countplot(x='sucesso_venda', data=df)
plt.title("Distribuição do Sucesso da Venda")
plt.show()
plt.close()

'''
A distribuição dos labels mostra que existem 2 classes possíveis:
0 = venda não concluída e 1 = venda concluída.
A próxima etapa é investigar a correlação entre as variáveis numéricas.
'''

# Correlação entre as features numéricas
numerics = ['int16', 'int32', 'int64', 'float16', 'float32', 'float64']
plt.figure(figsize=(10, 6))
sns.heatmap(df.select_dtypes(include=numerics).corr(), annot=True, cmap='coolwarm')
plt.title("Matriz de Correlação")
plt.show()
plt.close()

'''
Nesse cenário a base não possui problemas graves, mas se houvessem dados faltantes ou
duplicados é possível ajustar com os próximos comandos.
'''

# Remover dados duplicados, se houver.
df = df.drop_duplicates()

# Tratar outliers, se necessário, substituindo valores muito extremos pela mediana.
numeric_cols = ['mes_venda', 'qtd_garrafas', 'desconto_aplicado']

for col in numeric_cols:
    median = df[col].median()
    df[col] = df[col].apply(
        lambda x: median
        if x > df[col].quantile(0.975) or x < df[col].quantile(0.025)
        else x
    )

'''
Agora vai começar a engenharia de features, serão realizados os seguintes passos:
1. Separa as features X do label y;
2. Criar uma codificação para os dados em string via OneHotEncoder;
3. Remover as colunas originais antes dos tratamentos;
4. Separar os dados em treino (80%) e teste (20%);
5. Normalizar as features numéricas, sempre "aprendendo" a fórmula do treino e aplicando
no treino e no teste.
'''

# Separando features e labels
X = df.drop('sucesso_venda', axis=1)
y = df['sucesso_venda']

# Lista de colunas categóricas e aplicação de One-Hot Encoding
categorical_cols = [
    'tipo_vinho',
    'faixa_preco',
    'canal_venda',
    'regiao_cliente',
    'perfil_comprador',
]

ohe = OneHotEncoder(handle_unknown='ignore', sparse_output=False)
X_encoded = pd.DataFrame(ohe.fit_transform(X[categorical_cols]))

X_encoded = X_encoded.add_prefix('OHE_')

# Removendo colunas categóricas originais
X = X.drop(categorical_cols, axis=1)

# Concatenando as features codificadas com as numéricas
X = pd.concat([X.reset_index(drop=True), X_encoded.reset_index(drop=True)], axis=1)

# Dividindo os dados em conjuntos de treino e teste (80% treino, 20% teste)
X_train, X_test, y_train, y_test = train_test_split(
    X,
    y,
    test_size=0.2,
    random_state=42,
    stratify=y,
)

# Normalização das features numéricas
scaler = MinMaxScaler()
X_train_scaled = scaler.fit_transform(X_train)
X_teste_scaled = scaler.transform(X_test)

'''
Agora é possível aplicar os dados tratados em modelos preditivos.
Vou aplicar em Regressão Logística e em KNN usando K = 9.
'''

# Regressão Logística
logreg = LogisticRegression()
logreg.fit(X_train_scaled, y_train)
y_pred_logreg = logreg.predict(X_teste_scaled)
print("Acurácia Regressão Logística:", accuracy_score(y_test, y_pred_logreg))
print(classification_report(y_test, y_pred_logreg))
print(confusion_matrix(y_test, y_pred_logreg))

# KNN
knn = KNeighborsClassifier(n_neighbors=9)
knn.fit(X_train_scaled, y_train)
y_pred_knn = knn.predict(X_teste_scaled)
print("Acurácia KNN:", accuracy_score(y_test, y_pred_knn))
print(classification_report(y_test, y_pred_knn))
print(confusion_matrix(y_test, y_pred_knn))

'''
Agora vou aplicar o modelo SVM, para comparar o resultado com modelos mais complexos.
'''

# SVM com kernel RBF -> mais complexo
svm_rbf = SVC(kernel='rbf')
svm_rbf.fit(X_train_scaled, y_train)
y_pred_svm_rbf = svm_rbf.predict(X_teste_scaled)
print("Acurácia SVM (RBF):", accuracy_score(y_test, y_pred_svm_rbf))
print(classification_report(y_test, y_pred_svm_rbf))
print(confusion_matrix(y_test, y_pred_svm_rbf))

# SVM com kernel polinomial -> intermediário
svm_poly = SVC(kernel='poly')
svm_poly.fit(X_train_scaled, y_train)
y_pred_svm_poly = svm_poly.predict(X_teste_scaled)
print("Acurácia SVM (Polinomial):", accuracy_score(y_test, y_pred_svm_poly))
print(classification_report(y_test, y_pred_svm_poly))
print(confusion_matrix(y_test, y_pred_svm_poly))

# SVM com kernel linear -> o mais simples
svm_linear = SVC(kernel='linear')
svm_linear.fit(X_train_scaled, y_train)
y_pred_svm_linear = svm_linear.predict(X_teste_scaled)
print("Acurácia SVM (Linear):", accuracy_score(y_test, y_pred_svm_linear))
print(classification_report(y_test, y_pred_svm_linear))
print(confusion_matrix(y_test, y_pred_svm_linear))

'''
Agora vou aplicar modelos baseados em árvores, para comparação com os outros modelos.
'''

# Decision Tree
dt = DecisionTreeClassifier()
dt.fit(X_train_scaled, y_train)
y_pred_dt = dt.predict(X_teste_scaled)
print("Acurácia Decision Tree:", accuracy_score(y_test, y_pred_dt))
print(classification_report(y_test, y_pred_dt))
print(confusion_matrix(y_test, y_pred_dt))

# Random Forest
rf = RandomForestClassifier(n_estimators=25)
rf.fit(X_train_scaled, y_train)
y_pred_rf = rf.predict(X_teste_scaled)
print("Acurácia Random Forest:", accuracy_score(y_test, y_pred_rf))
print(classification_report(y_test, y_pred_rf))
print(confusion_matrix(y_test, y_pred_rf))

'''
Dentre os modelos testados, basta comparar as acurácias impressas no terminal para escolher
o melhor modelo para prever o sucesso de venda.
'''
