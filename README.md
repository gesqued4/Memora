<h1>Memora</h1>

  <p><strong>Memora</strong> is a simple app built for storing and organizing your personal memories. You can add images, titles, descriptions, and emojis, making it a fun way to keep track of your moments. This project is developed as a school project using circular doubly linked lists for efficient memory storage.</p>

  <h2>🚀 Features</h2>
  <ul>
    <li><strong>Drag and Drop</strong>: Easily add images to the app with drag-and-drop functionality using the <strong>DND package</strong>.</li>
    <li><strong>Emoji Parsing</strong>: Thanks to <strong>Vincent DURMONT's library</strong>, we can parse emoji aliases (e.g., <code>:smile:</code>) to their actual emoji representation! 🎉</li>
    <li><strong>Memory Management</strong>: Store memories with images, titles, descriptions, emojis, and the date.</li>
    <li><strong>Circular Doubly Linked List</strong>: All memories are stored in a circular doubly linked list for efficient retrieval and organization.</li>
  </ul>

  ##Sign In & Sign Up  
  <br>
  <div align="center">
      <img src="Screenshots/signIn_SS.png" alt="Sign In screen" width="200" />
      <img src="Screenshots/signUp_SS.png" alt="Sign Up screen" width="200" />
  </div>
  <br>
  <p>Sign In screen where users can log into their account and create a new account.</p>
  ---

  <h2>Add memories</h2>  
  <br>
  <div align="center">
      <img src="Screenshots/addMemories_SS.png" alt="Add Memories screen 1" width="200" />
      <img src="Screenshots/addMemories_SS_2.png" alt="Add Memories screen 2" width="200" />
  </div>
  <br>
  <p>Add Memories screen where users can drag and drop images, add titles, descriptions, and emojis by their aliases.</p>
  ---

  <h2>Memories & All memories</h2>
  <br>
  <div align="center">
    <img src="Screenshots/memories_SS.png" alt="Last 5 nodes" width="200" />
    <img src="Screenshots/allMemories_SS.png" alt="All Memories screen" width="200" />
  </div>
  <br>
  <p>View the last 5 memories shown as "LIFO" type node and a random suggestion from the list also you can see all the added memories, with options to delete nodes, go to next/previous images, and jump to the beginning/end.</p>
  ---

  <h2>🐞 Known Issues</h2>
  <p>Please note, there are a few features that aren't yet fully implemented, but they are not bugs—just missing functionality for now:</p>
  <ul>
    <li><strong>User Management</strong>: The system would allow each user to have their own set of memories stored securely.</li>
    <li><strong>Memory Filtering</strong>: Right now, all memories from different users are shown. In the future, we plan to filter memories by the user, so you’ll only see your own memories in the app.</li>
  </ul>

  <h2>🛠️ Technologies Used</h2>
  <ul>
    <li><strong>DND Package</strong>: For drag-and-drop functionality to add images seamlessly.</li>
    <li><strong>Vincent DURMONT's Emoji Parser</strong>: To turn emoji aliases into actual emojis. <a href="https://github.com/vdurmont/emoji-java">Library link here</a></li>
    <li><strong>Circular Doubly Linked List</strong>: For efficient memory storage</li>
  </ul>

  <h2>🙌 Credits</h2>
  <ul>
    <li>Special thanks to <strong>Vincent DURMONT</strong> for the library that makes emoji parsing possible!</li>
  </ul>
