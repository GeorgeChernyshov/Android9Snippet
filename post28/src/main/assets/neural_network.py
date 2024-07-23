import pandas as pd

images = pd.read_csv("images.csv")
labels = pd.read_csv("labels.csv")
test_images = pd.read_csv("t10k-images.csv")
test_labels = pd.read_csv("t10k-labels.csv")

labels = pd.get_dummies(labels['5'])
test_labels = pd.get_dummies(test_labels['7'])

def print_image(image, width, height):
  for i in range(height):
    str = ""
    for j in range(width):
      if image[(width * i) + j] > 180:
        str += "X"
      else: 
        str += "."
    print(str)

def check_image_size(images):
  first_image = images.values[0]
  print(first_image)

  for i in range(1, len(first_image)):
    if len(first_image) % i == 0:
      j = len(first_image) / i
      print(f'width = {j}, height = {i}')
      print_image(first_image, int(j), int(i))

# check_image_size(images)

from tensorflow import keras
# from tensorflow.keras import layers

model = keras.Sequential([
  keras.layers.Input(shape=(images.shape[1],)),
  keras.layers.Dense(10, activation='softmax')
])

model.compile(optimizer='RMSprop',
  loss='binary_crossentropy',
  metrics=['accuracy'])

model.fit(images, labels, epochs=10, verbose=0)

loss, test_acc = model.evaluate(test_images, test_labels, verbose=0)
print(test_acc)

for layer in model.layers: print(layer.get_config(), layer.get_weights()[0].tolist())

# i = 0
# for image in test_images.values:
#   print_image(image, 28, 28)
#   print(model.predict(image.reshape(1, 784)))
#   print(test_labels.values[i])
#   i = i + 1